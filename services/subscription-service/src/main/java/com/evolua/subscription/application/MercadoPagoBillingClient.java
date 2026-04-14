package com.evolua.subscription.application;

import com.evolua.subscription.config.BillingProperties;
import com.evolua.subscription.domain.PlanCatalogItem;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class MercadoPagoBillingClient {
  private final RestClient restClient;

  public MercadoPagoBillingClient(BillingProperties billingProperties) {
    this.restClient =
        RestClient.builder()
            .baseUrl(billingProperties.getMercadoPago().getBaseUrl())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + billingProperties.getMercadoPago().getAccessToken())
            .build();
  }

  public MercadoPagoPreference createCheckoutPreference(
      String checkoutId,
      PlanCatalogItem plan,
      String successUrl,
      String pendingUrl,
      String failureUrl,
      String notificationUrl) {
    var response =
        restClient
            .post()
            .uri("/checkout/preferences")
            .body(
                Map.of(
                    "items",
                    List.of(
                        Map.of(
                            "id", plan.planCode(),
                            "title", plan.title(),
                            "quantity", 1,
                            "currency_id", plan.currency(),
                            "unit_price", plan.price())),
                    "external_reference",
                    checkoutId,
                    "notification_url",
                    notificationUrl,
                    "back_urls",
                    Map.of("success", successUrl, "pending", pendingUrl, "failure", failureUrl),
                    "auto_return",
                    "approved"))
            .retrieve()
            .body(MercadoPagoPreferenceResponse.class);

    if (response == null || response.id() == null || response.initPoint() == null) {
      throw new IllegalArgumentException("Mercado Pago did not return a valid checkout URL");
    }
    return new MercadoPagoPreference(response.id(), response.initPoint());
  }

  public MercadoPagoPayment fetchPayment(Long paymentId) {
    var response =
        restClient.get().uri("/v1/payments/{id}", paymentId).retrieve().body(MercadoPagoPaymentResponse.class);
    if (response == null || response.id() == null || response.status() == null) {
      throw new IllegalArgumentException("Mercado Pago did not return a valid payment");
    }
    var customerId =
        response.payer() == null || response.payer().id() == null
            ? null
            : String.valueOf(response.payer().id());
    return new MercadoPagoPayment(
        String.valueOf(response.id()),
        response.status(),
        response.externalReference(),
        customerId,
        response.transactionAmount() == null ? BigDecimal.ZERO : response.transactionAmount());
  }

  public record MercadoPagoPreference(String preferenceId, String checkoutUrl) {}

  public record MercadoPagoPayment(
      String paymentId,
      String status,
      String checkoutPublicId,
      String customerId,
      BigDecimal amount) {}

  private record MercadoPagoPreferenceResponse(
      String id, @JsonProperty("init_point") String initPoint) {}

  private record MercadoPagoPaymentResponse(
      Long id,
      String status,
      @JsonProperty("external_reference") String externalReference,
      @JsonProperty("transaction_amount") BigDecimal transactionAmount,
      MercadoPagoPayer payer) {}

  private record MercadoPagoPayer(Long id) {}
}
