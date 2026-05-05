package com.evolua.subscription.application;

import com.evolua.subscription.config.BillingProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AdMobSsvVerifier {
  private static final Duration KEY_CACHE_TTL = Duration.ofHours(24);
  private static final String SIGNATURE_PARAM = "signature=";
  private static final String KEY_ID_PARAM = "key_id=";

  private final BillingProperties billingProperties;
  private final ObjectMapper objectMapper;
  private final RestClient restClient;
  private Map<String, PublicKey> cachedKeys = Map.of();
  private Instant cachedAt = Instant.EPOCH;

  public AdMobSsvVerifier(BillingProperties billingProperties, ObjectMapper objectMapper) {
    this.billingProperties = billingProperties;
    this.objectMapper = objectMapper;
    this.restClient = RestClient.builder().build();
  }

  public void verify(String queryString) {
    if (!billingProperties.getAdmob().isSsvVerificationEnabled()) {
      return;
    }
    try {
      verify(queryString, publicKeys());
    } catch (GeneralSecurityException exception) {
      throw new IllegalArgumentException("Invalid AdMob rewarded callback signature", exception);
    }
  }

  void verify(String queryString, Map<String, PublicKey> publicKeys) throws GeneralSecurityException {
    if (queryString == null || queryString.isBlank()) {
      throw new GeneralSecurityException("Missing query string");
    }
    var signatureIndex = queryString.indexOf(SIGNATURE_PARAM);
    if (signatureIndex <= 0) {
      throw new GeneralSecurityException("Missing signature");
    }
    var signedContent = queryString.substring(0, signatureIndex - 1).getBytes(StandardCharsets.UTF_8);
    var signatureAndKey = queryString.substring(signatureIndex);
    var keyIdIndex = signatureAndKey.indexOf(KEY_ID_PARAM);
    if (keyIdIndex <= SIGNATURE_PARAM.length()) {
      throw new GeneralSecurityException("Missing key_id");
    }
    var signatureText = signatureAndKey.substring(SIGNATURE_PARAM.length(), keyIdIndex - 1);
    var keyId = signatureAndKey.substring(keyIdIndex + KEY_ID_PARAM.length());
    var publicKey = publicKeys.get(keyId);
    if (publicKey == null) {
      throw new GeneralSecurityException("Unknown AdMob key_id");
    }

    var verifier = Signature.getInstance("SHA256withECDSA");
    verifier.initVerify(publicKey);
    verifier.update(signedContent);
    if (!verifier.verify(decodeUrlSafeBase64(signatureText))) {
      throw new GeneralSecurityException("Signature mismatch");
    }
  }

  private Map<String, PublicKey> publicKeys() throws GeneralSecurityException {
    if (!cachedKeys.isEmpty() && cachedAt.plus(KEY_CACHE_TTL).isAfter(Instant.now())) {
      return cachedKeys;
    }
    var keyJson =
        restClient
            .get()
            .uri(billingProperties.getAdmob().getVerifierKeysUrl())
            .retrieve()
            .body(String.class);
    var parsed = parsePublicKeys(keyJson);
    cachedKeys = parsed;
    cachedAt = Instant.now();
    return parsed;
  }

  @SuppressWarnings("unchecked")
  Map<String, PublicKey> parsePublicKeys(String keyJson) throws GeneralSecurityException {
    try {
      var payload = objectMapper.readValue(keyJson == null ? "{}" : keyJson, Map.class);
      var rawKeys = payload.get("keys") instanceof List<?> list ? list : List.of();
      var keys = new HashMap<String, PublicKey>();
      var keyFactory = KeyFactory.getInstance("EC");
      for (var rawKey : rawKeys) {
        if (!(rawKey instanceof Map<?, ?> item)) {
          continue;
        }
        var keyId = String.valueOf(item.get("keyId"));
        var keyMaterial = item.get("base64") == null ? "" : String.valueOf(item.get("base64"));
        if (keyId.isBlank() || keyMaterial.isBlank()) {
          continue;
        }
        keys.put(keyId, keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(keyMaterial))));
      }
      if (keys.isEmpty()) {
        throw new GeneralSecurityException("No AdMob verifier keys available");
      }
      return keys;
    } catch (GeneralSecurityException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new GeneralSecurityException("Could not parse AdMob verifier keys", exception);
    }
  }

  private byte[] decodeUrlSafeBase64(String value) {
    var padded = value;
    var remainder = padded.length() % 4;
    if (remainder > 0) {
      padded += "=".repeat(4 - remainder);
    }
    return Base64.getUrlDecoder().decode(padded);
  }
}
