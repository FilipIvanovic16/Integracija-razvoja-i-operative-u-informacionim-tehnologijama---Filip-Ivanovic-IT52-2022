package com.chronoshop.payment.controller;

import com.chronoshop.dto.PaymentDtos.CheckoutSessionResponse;
import com.chronoshop.dto.PaymentDtos.CreatePaymentIntentRequest;
import com.chronoshop.payment.security.SecurityUtils;
import com.chronoshop.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

  private final PaymentService paymentService;

  public PaymentController(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  /** Kupac inicira plaćanje za svoju porudžbinu i dobija client secret za Stripe. */
  @PostMapping("/create-intent")
  public CheckoutSessionResponse createIntent(
      @Valid @RequestBody CreatePaymentIntentRequest request) {
    return paymentService.createPaymentIntent(
        SecurityUtils.currentUserId(), SecurityUtils.isAdmin(), request.orderId());
  }

  /**
   * Stripe webhook — javni endpoint koji Stripe serveri pozivaju asinhrono. Potpis se verifikuje u
   * servisu. Mora primati sirovo telo zahteva.
   */
  @PostMapping("/webhook")
  public ResponseEntity<String> webhook(
      @RequestBody String payload, @RequestHeader("Stripe-Signature") String signature) {
    paymentService.handleWebhook(payload, signature);
    return ResponseEntity.ok("OK");
  }
}
