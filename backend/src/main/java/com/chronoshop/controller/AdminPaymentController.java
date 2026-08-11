package com.chronoshop.controller;

import com.chronoshop.domain.enums.PaymentStatus;
import com.chronoshop.dto.PageResponse;
import com.chronoshop.dto.PaymentDtos.PaymentResponse;
import com.chronoshop.service.PaymentService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Pregled transakcija u admin panelu — podaci se popunjavaju kroz Stripe webhook.
 * Prikazuje koji proizvod je kupljen, po kojoj ceni, od kog kupca i u kom statusu.
 */
@RestController
@RequestMapping("/api/admin/payments")
public class AdminPaymentController {

    private final PaymentService paymentService;

    public AdminPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public PageResponse<PaymentResponse> transactions(
            @RequestParam(required = false) PaymentStatus status,
            @PageableDefault(size = 15, sort = "createdAt") Pageable pageable) {
        return paymentService.listTransactions(status, pageable);
    }
}
