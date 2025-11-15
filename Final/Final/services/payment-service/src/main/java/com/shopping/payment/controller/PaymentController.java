package com.shopping.payment.controller;

import com.shopping.common.ApiResponse;
import com.shopping.payment.controller.dto.*;
import com.shopping.payment.model.Payment;
import com.shopping.payment.model.PaymentStatus;
import com.shopping.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "APIs for managing payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Create a new payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Creating payment for order: {}, user: {}", request.getOrderId(), userId);

        Payment payment = paymentService.createPayment(
                request.getOrderId(),
                userId,
                request.getAmount(),
                request.getPaymentMethod(),
                request.getIdempotencyKey(),
                request.getCurrency()
        );

        PaymentResponse response = mapToPaymentResponse(payment);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment created successfully", response));
    }

    @PostMapping("/{paymentId}/process")
    @Operation(summary = "Process a payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @PathVariable String paymentId,
            @Valid @RequestBody ProcessPaymentRequest request) {

        log.info("Processing payment: {}", paymentId);

        Payment payment = paymentService.processPayment(paymentId, request.getTransactionId());
        PaymentResponse response = mapToPaymentResponse(payment);

        return ResponseEntity.ok(ApiResponse.success("Payment processed successfully", response));
    }

    @PostMapping("/{paymentId}/cancel")
    @Operation(summary = "Cancel a payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(
            @PathVariable String paymentId) {

        log.info("Canceling payment: {}", paymentId);

        Payment payment = paymentService.cancelPayment(paymentId);
        PaymentResponse response = mapToPaymentResponse(payment);

        return ResponseEntity.ok(ApiResponse.success("Payment cancelled successfully", response));
    }

    @PostMapping("/{paymentId}/refund")
    @Operation(summary = "Refund a payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(
            @PathVariable String paymentId,
            @Valid @RequestBody RefundRequest request) {

        log.info("Processing refund for payment: {}, amount: {}", paymentId, request.getAmount());

        Payment payment = paymentService.refundPayment(paymentId, request.getAmount());
        PaymentResponse response = mapToPaymentResponse(payment);

        return ResponseEntity.ok(ApiResponse.success("Payment refunded successfully", response));
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @PathVariable String paymentId) {

        log.info("Fetching payment: {}", paymentId);

        Payment payment = paymentService.getPaymentById(paymentId);
        PaymentResponse response = mapToPaymentResponse(payment);

        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", response));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get payments by user ID")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByUser(
            @PathVariable Long userId) {

        log.info("Fetching payments for user: {}", userId);

        List<Payment> payments = paymentService.getPaymentsByUserId(userId);
        List<PaymentResponse> responses = payments.stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", responses));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payments by order ID")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByOrder(
            @PathVariable Long orderId) {

        log.info("Fetching payments for order: {}", orderId);

        List<Payment> payments = paymentService.getPaymentsByOrderId(orderId);
        List<PaymentResponse> responses = payments.stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", responses));
    }

    @PutMapping("/{paymentId}/status")
    @Operation(summary = "Update payment status")
    public ResponseEntity<ApiResponse<PaymentResponse>> updatePaymentStatus(
            @PathVariable String paymentId,
            @Valid @RequestBody UpdatePaymentStatusRequest request) {

        log.info("Updating payment status: {} to {}", paymentId, request.getStatus());

        Payment payment = paymentService.updatePaymentStatus(
                paymentId,
                request.getStatus(),
                request.getTransactionId()
        );
        PaymentResponse response = mapToPaymentResponse(payment);

        return ResponseEntity.ok(ApiResponse.success("Payment status updated successfully", response));
    }

    @GetMapping("/{paymentId}/validate")
    @Operation(summary = "Validate if payment is completed")
    public ResponseEntity<ApiResponse<Boolean>> validatePayment(
            @PathVariable String paymentId) {

        log.info("Validating payment: {}", paymentId);

        boolean isValid = paymentService.validatePayment(paymentId);

        return ResponseEntity.ok(ApiResponse.success(isValid));
    }

    @GetMapping("/order/{orderId}/status/{status}")
    @Operation(summary = "Find payment by order ID and status")
    public ResponseEntity<ApiResponse<PaymentResponse>> findPaymentByOrderAndStatus(
            @PathVariable Long orderId,
            @PathVariable PaymentStatus status) {

        log.info("Finding payment for order: {} with status: {}", orderId, status);

        Payment payment = paymentService.findByOrderIdAndStatus(orderId, status);
        PaymentResponse response = mapToPaymentResponse(payment);

        return ResponseEntity.ok(ApiResponse.success("Payment found successfully", response));
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .currency(payment.getCurrency())
                .description(payment.getDescription())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .completedAt(payment.getCompletedAt())
                .failureReason(payment.getFailureReason())
                .build();
    }
}