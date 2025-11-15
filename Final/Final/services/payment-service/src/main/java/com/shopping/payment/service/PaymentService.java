package com.shopping.payment.service;

import com.shopping.payment.model.Payment;
import com.shopping.payment.model.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {

    Payment createPayment(Long orderId, Long userId, BigDecimal amount,
                          String paymentMethod, String idempotencyKey, String currency);

    Payment processPayment(String paymentId, String transactionId);

    Payment cancelPayment(String paymentId);

    Payment refundPayment(String paymentId, BigDecimal refundAmount);

    Payment getPaymentById(String paymentId);

    List<Payment> getPaymentsByUserId(Long userId);

    List<Payment> getPaymentsByOrderId(Long orderId);

    Payment updatePaymentStatus(String paymentId, PaymentStatus status, String transactionId);

    boolean validatePayment(String paymentId);

    Payment findByOrderIdAndStatus(Long orderId, PaymentStatus status);
}