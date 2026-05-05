package com.evolua.subscription.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.billing")
public class BillingProperties {
  private String provider = "mercadopago";
  private String frontendBaseUrl = "http://localhost:3000";
  private String publicBaseUrl = "http://localhost:8087";
  private String internalToken = "change-me-internal-token";
  private MercadoPago mercadoPago = new MercadoPago();
  private AdMob admob = new AdMob();

  public String getProvider() { return provider; }
  public void setProvider(String provider) { this.provider = provider; }
  public String getFrontendBaseUrl() { return frontendBaseUrl; }
  public void setFrontendBaseUrl(String frontendBaseUrl) { this.frontendBaseUrl = frontendBaseUrl; }
  public String getPublicBaseUrl() { return publicBaseUrl; }
  public void setPublicBaseUrl(String publicBaseUrl) { this.publicBaseUrl = publicBaseUrl; }
  public String getInternalToken() { return internalToken; }
  public void setInternalToken(String internalToken) { this.internalToken = internalToken; }
  public MercadoPago getMercadoPago() { return mercadoPago; }
  public void setMercadoPago(MercadoPago mercadoPago) { this.mercadoPago = mercadoPago; }
  public AdMob getAdmob() { return admob; }
  public void setAdmob(AdMob admob) { this.admob = admob; }

  public static class MercadoPago {
    private String baseUrl = "https://api.mercadopago.com";
    private String accessToken = "";
    private String webhookSecret = "";

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getWebhookSecret() { return webhookSecret; }
    public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }
  }

  public static class AdMob {
    private String verifierKeysUrl = "https://www.gstatic.com/admob/reward/verifier-keys.json";
    private boolean ssvVerificationEnabled = true;

    public String getVerifierKeysUrl() { return verifierKeysUrl; }
    public void setVerifierKeysUrl(String verifierKeysUrl) { this.verifierKeysUrl = verifierKeysUrl; }
    public boolean isSsvVerificationEnabled() { return ssvVerificationEnabled; }
    public void setSsvVerificationEnabled(boolean ssvVerificationEnabled) {
      this.ssvVerificationEnabled = ssvVerificationEnabled;
    }
  }
}
