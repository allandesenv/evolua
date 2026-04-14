package com.evolua.subscription.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "billing_checkouts")
public class BillingCheckoutEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String publicId;
  private String userId;
  private String planCode;
  private String billingCycle;
  private String provider;
  private String status;
  private Boolean premium;
  private String providerPreferenceId;
  private String providerPaymentId;
  @Column(columnDefinition = "TEXT")
  private String checkoutUrl;
  private String failureReason;
  private Instant createdAt;
  private Instant updatedAt;
  private Instant confirmedAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getPublicId() { return publicId; }
  public void setPublicId(String publicId) { this.publicId = publicId; }
  public String getUserId() { return userId; }
  public void setUserId(String userId) { this.userId = userId; }
  public String getPlanCode() { return planCode; }
  public void setPlanCode(String planCode) { this.planCode = planCode; }
  public String getBillingCycle() { return billingCycle; }
  public void setBillingCycle(String billingCycle) { this.billingCycle = billingCycle; }
  public String getProvider() { return provider; }
  public void setProvider(String provider) { this.provider = provider; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public Boolean getPremium() { return premium; }
  public void setPremium(Boolean premium) { this.premium = premium; }
  public String getProviderPreferenceId() { return providerPreferenceId; }
  public void setProviderPreferenceId(String providerPreferenceId) { this.providerPreferenceId = providerPreferenceId; }
  public String getProviderPaymentId() { return providerPaymentId; }
  public void setProviderPaymentId(String providerPaymentId) { this.providerPaymentId = providerPaymentId; }
  public String getCheckoutUrl() { return checkoutUrl; }
  public void setCheckoutUrl(String checkoutUrl) { this.checkoutUrl = checkoutUrl; }
  public String getFailureReason() { return failureReason; }
  public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
  public Instant getConfirmedAt() { return confirmedAt; }
  public void setConfirmedAt(Instant confirmedAt) { this.confirmedAt = confirmedAt; }
}
