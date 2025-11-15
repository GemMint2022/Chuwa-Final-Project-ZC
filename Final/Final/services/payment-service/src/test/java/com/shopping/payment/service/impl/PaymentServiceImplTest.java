package com.shopping.payment.service.impl;

import com.shopping.common.BusinessException;
import com.shopping.payment.event.producer.PaymentEventProducer;
import com.shopping.payment.model.Payment;
import com.shopping.payment.model.PaymentStatus;
import com.shopping.payment.repository.PaymentRepository;
import com.shopping.payment.service.client.AccountServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AccountServiceClient accountServiceClient;

    @Mock
    private PaymentEventProducer paymentEventProducer;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment testPayment;

    @BeforeEach
    void setUp() {
        testPayment = Payment.builder()
                .id(1L)
                .paymentId("pay_1234567890abcdef")
                .orderId(1001L)
                .userId(5001L)
                .amount(new BigDecimal("99.99"))
                .status(PaymentStatus.PENDING)
                .currency("USD")
                .idempotencyKey("idemp_12345")
                .build();
    }

    @Test
    void createPayment_Success() {
        // Given
        when(paymentRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(paymentRepository.existsByOrderIdAndStatusIn(any(), any())).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        Payment result = paymentService.createPayment(
                1001L, 5001L, new BigDecimal("99.99"),
                "CREDIT_CARD", "idemp_12345", "USD"
        );

        // Then
        assertNotNull(result);
        assertEquals("pay_1234567890abcdef", result.getPaymentId());
        assertEquals(PaymentStatus.PENDING, result.getStatus());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void createPayment_DuplicateIdempotencyKey_ThrowsException() {
        // Given
        when(paymentRepository.findByIdempotencyKey("idemp_12345"))
                .thenReturn(Optional.of(testPayment));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paymentService.createPayment(
                    1001L, 5001L, new BigDecimal("99.99"),
                    "CREDIT_CARD", "idemp_12345", "USD"
            );
        });

        assertEquals("DUPLICATE_PAYMENT_REQUEST", exception.getErrorCode());
    }

    @Test
    void createPayment_InvalidAmount_ThrowsException() {
        // Given
        when(paymentRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(paymentRepository.existsByOrderIdAndStatusIn(any(), any())).thenReturn(false);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paymentService.createPayment(
                    1001L, 5001L, new BigDecimal("-10.00"),
                    "CREDIT_CARD", "idemp_12345", "USD"
            );
        });

        assertEquals("INVALID_PAYMENT_AMOUNT", exception.getErrorCode());
    }

    @Test
    void processPayment_Success() {
        // Given
        testPayment.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findByPaymentId("pay_1234567890abcdef"))
                .thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        Payment result = paymentService.processPayment("pay_1234567890abcdef", "txn_12345");

        // Then
        assertNotNull(result);
        assertEquals(PaymentStatus.COMPLETED, result.getStatus());
        assertEquals("txn_12345", result.getTransactionId());
        verify(paymentEventProducer, times(1)).sendPaymentCompletedEvent(any(Payment.class));
    }

    @Test
    void processPayment_AlreadyProcessed_ThrowsException() {
        // Given
        testPayment.setStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findByPaymentId("pay_1234567890abcdef"))
                .thenReturn(Optional.of(testPayment));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paymentService.processPayment("pay_1234567890abcdef", "txn_12345");
        });

        assertEquals("PAYMENT_ALREADY_PROCESSED", exception.getErrorCode());
    }

    @Test
    void cancelPayment_Success() {
        // Given
        testPayment.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findByPaymentId("pay_1234567890abcdef"))
                .thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        Payment result = paymentService.cancelPayment("pay_1234567890abcdef");

        // Then
        assertNotNull(result);
        assertEquals(PaymentStatus.CANCELLED, result.getStatus());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void getPaymentById_NotFound_ThrowsException() {
        // Given
        when(paymentRepository.findByPaymentId("nonexistent_payment"))
                .thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paymentService.getPaymentById("nonexistent_payment");
        });

        assertEquals("PAYMENT_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    void validatePayment_CompletedPayment_ReturnsTrue() {
        // Given
        testPayment.setStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findByPaymentId("pay_1234567890abcdef"))
                .thenReturn(Optional.of(testPayment));

        // When
        boolean isValid = paymentService.validatePayment("pay_1234567890abcdef");

        // Then
        assertTrue(isValid);
    }

    @Test
    void validatePayment_PendingPayment_ReturnsFalse() {
        // Given
        testPayment.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findByPaymentId("pay_1234567890abcdef"))
                .thenReturn(Optional.of(testPayment));

        // When
        boolean isValid = paymentService.validatePayment("pay_1234567890abcdef");

        // Then
        assertFalse(isValid);
    }
}