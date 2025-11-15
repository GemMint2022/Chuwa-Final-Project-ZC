package com.shopping.payment.service.impl;

import com.shopping.common.BusinessException;
import com.shopping.common.security.JwtTokenUtil;
import com.shopping.payment.event.producer.PaymentEventProducer;
import com.shopping.payment.model.Payment;
import com.shopping.payment.model.PaymentMethod;
import com.shopping.payment.model.PaymentStatus;
import com.shopping.payment.repository.PaymentRepository;
import com.shopping.payment.service.PaymentService;
import com.shopping.payment.service.client.AccountServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;
    // 移除 AccountServiceClient

    @Override
    @Transactional
    public Payment createPayment(Long orderId, Long userId, BigDecimal amount,
                                 String paymentMethod, String idempotencyKey, String currency) {

        validatePaymentCreation(orderId, userId, amount, idempotencyKey);

        log.info("Creating payment for user: {}, order: {}", userId, orderId);

        Payment payment = Payment.builder()
                .paymentId(generatePaymentId())
                .orderId(orderId)
                .userId(userId)
                .amount(amount)
                .paymentMethod(PaymentMethod.valueOf(paymentMethod.toUpperCase()))
                .status(PaymentStatus.PENDING)
                .currency(currency)
                .idempotencyKey(idempotencyKey)
                .description("Payment for order: " + orderId)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Created payment: {} for order: {} and user: {}",
                savedPayment.getPaymentId(), orderId, userId);

        return savedPayment;
    }

    @Override
    @Transactional
    public Payment processPayment(String paymentId, String transactionId) {
        Payment payment = getPaymentById(paymentId);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException("Payment already processed",
                    "PAYMENT_ALREADY_PROCESSED", HttpStatus.CONFLICT);
        }

        // Simulate payment processing with external payment gateway
        boolean paymentSuccess = simulatePaymentProcessing(payment);

        if (paymentSuccess) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setTransactionId(transactionId);
            payment.setCompletedAt(LocalDateTime.now());

            Payment updatedPayment = paymentRepository.save(payment);

            // Send payment completed event
            paymentEventProducer.sendPaymentCompletedEvent(updatedPayment);
            log.info("Payment processed successfully: {}", paymentId);

            return updatedPayment;
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Payment gateway declined transaction");

            Payment failedPayment = paymentRepository.save(payment);

            // Send payment failed event
            paymentEventProducer.sendPaymentFailedEvent(failedPayment, "Payment gateway declined");
            log.warn("Payment processing failed: {}", paymentId);

            throw new BusinessException("Payment processing failed",
                    "PAYMENT_PROCESSING_FAILED", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @Override
    @Transactional
    public Payment cancelPayment(String paymentId) {
        Payment payment = getPaymentById(paymentId);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException("Only pending payments can be cancelled",
                    "PAYMENT_NOT_CANCELLABLE", HttpStatus.BAD_REQUEST);
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setFailureReason("Payment cancelled by user");

        Payment cancelledPayment = paymentRepository.save(payment);
        log.info("Payment cancelled: {}", paymentId);

        return cancelledPayment;
    }

    @Override
    @Transactional
    public Payment refundPayment(String paymentId, BigDecimal refundAmount) {
        Payment payment = getPaymentById(paymentId);

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BusinessException("Only completed payments can be refunded",
                    "PAYMENT_NOT_REFUNDABLE", HttpStatus.BAD_REQUEST);
        }

        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new BusinessException("Refund amount cannot exceed original payment amount",
                    "INVALID_REFUND_AMOUNT", HttpStatus.BAD_REQUEST);
        }

        // Simulate refund processing
        boolean refundSuccess = simulateRefundProcessing(payment, refundAmount);

        if (refundSuccess) {
            payment.setStatus(PaymentStatus.REFUNDED);

            Payment refundedPayment = paymentRepository.save(payment);
            log.info("Payment refunded: {}, amount: {}", paymentId, refundAmount);

            return refundedPayment;
        } else {
            throw new BusinessException("Refund processing failed",
                    "REFUND_PROCESSING_FAILED", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public Payment getPaymentById(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new BusinessException("Payment not found: " + paymentId,
                        "PAYMENT_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    @Override
    public List<Payment> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

    @Override
    public List<Payment> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Override
    @Transactional
    public Payment updatePaymentStatus(String paymentId, PaymentStatus status, String transactionId) {
        Payment payment = getPaymentById(paymentId);

        payment.setStatus(status);
        if (transactionId != null) {
            payment.setTransactionId(transactionId);
        }
        if (status == PaymentStatus.COMPLETED) {
            payment.setCompletedAt(LocalDateTime.now());
        }

        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Updated payment status: {} to {}", paymentId, status);

        return updatedPayment;
    }

    @Override
    public boolean validatePayment(String paymentId) {
        try {
            Payment payment = getPaymentById(paymentId);
            return payment.getStatus() == PaymentStatus.COMPLETED;
        } catch (BusinessException e) {
            return false;
        }
    }

    @Override
    public Payment findByOrderIdAndStatus(Long orderId, PaymentStatus status) {
        return paymentRepository.findByOrderId(orderId).stream()
                .filter(payment -> payment.getStatus() == status)
                .findFirst()
                .orElseThrow(() -> new BusinessException("Payment not found for order: " + orderId + " with status: " + status,
                        "PAYMENT_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    private void validatePaymentCreation(Long orderId, Long userId, BigDecimal amount, String idempotencyKey) {
        if (idempotencyKey != null && paymentRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            throw new BusinessException("Duplicate payment request", "DUPLICATE_PAYMENT_REQUEST");
        }

        if (paymentRepository.existsByOrderIdAndStatusIn(orderId,
                List.of(PaymentStatus.COMPLETED, PaymentStatus.PROCESSING))) {
            throw new BusinessException("Payment already exists for this order", "PAYMENT_ALREADY_EXISTS");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Invalid payment amount", "INVALID_PAYMENT_AMOUNT");
        }

        log.debug("User validation assumed to be completed at API Gateway level");
    }


    private String generatePaymentId() {
        return "pay_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private boolean simulatePaymentProcessing(Payment payment) {
        // Simulate external payment gateway processing
        // In real implementation, this would integrate with Stripe, PayPal, etc.
        try {
            Thread.sleep(1000); // Simulate processing time
            // For demo purposes, assume payment succeeds if amount is positive
            return payment.getAmount().compareTo(BigDecimal.ZERO) > 0;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private boolean simulateRefundProcessing(Payment payment, BigDecimal refundAmount) {
        // Simulate refund processing with payment gateway
        try {
            Thread.sleep(500);
            return true; // Assume refund always succeeds for demo
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}