package com.evolua.subscription.application;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.evolua.subscription.config.BillingProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Base64;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AdMobSsvVerifierTest {
  @Test
  void verifiesSignedAdMobRewardQueryString() throws Exception {
    var keyPairGenerator = KeyPairGenerator.getInstance("EC");
    keyPairGenerator.initialize(256);
    var keyPair = keyPairGenerator.generateKeyPair();
    var verifier = new AdMobSsvVerifier(new BillingProperties(), new ObjectMapper());
    var keyJson =
        """
        {"keys":[{"keyId":123,"base64":"%s"}]}
        """
            .formatted(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
    var keys = verifier.parsePublicKeys(keyJson);
    var signedContent =
        "ad_network=5450213213286189855&ad_unit=2747237135&custom_data=session-1"
            + "&reward_amount=1&reward_item=ai_action&timestamp=150777823&transaction_id=tx-1";
    var query = signedContent + "&signature=" + sign(signedContent, keyPair.getPrivate()) + "&key_id=123";

    assertDoesNotThrow(() -> verifier.verify(query, keys));
  }

  @Test
  void rejectsTamperedAdMobRewardQueryString() throws Exception {
    var keyPairGenerator = KeyPairGenerator.getInstance("EC");
    keyPairGenerator.initialize(256);
    var keyPair = keyPairGenerator.generateKeyPair();
    var verifier = new AdMobSsvVerifier(new BillingProperties(), new ObjectMapper());
    var keys = Map.of("123", keyPair.getPublic());
    var signedContent = "custom_data=session-1&reward_amount=1&transaction_id=tx-1";
    var query = signedContent + "&signature=" + sign(signedContent, keyPair.getPrivate()) + "&key_id=123";
    var tampered = query.replace("reward_amount=1", "reward_amount=999");

    assertThrows(java.security.GeneralSecurityException.class, () -> verifier.verify(tampered, keys));
  }

  private String sign(String signedContent, java.security.PrivateKey privateKey) throws Exception {
    var signer = Signature.getInstance("SHA256withECDSA");
    signer.initSign(privateKey);
    signer.update(signedContent.getBytes(StandardCharsets.UTF_8));
    return Base64.getUrlEncoder().withoutPadding().encodeToString(signer.sign());
  }
}
