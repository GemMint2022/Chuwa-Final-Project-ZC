package com.shopping.payment.event.producer;

import com.shopping.payment.event.producer.event.PaymentCompletedEvent;
import com.shopping.payment.event.producer.event.PaymentFailedEvent;
import com.shopping.payment.model.Payment;
import com.shopping.payment.model.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private PaymentEventProducer paymentEventProducer;

    private Payment createTestPayment() {
        return Payment.builder()
                .paymentId("pay_test123")
                .orderId(1001L)
                .userId(5001L)
                .amount(new BigDecimal("99.99"))
                .status(PaymentStatus.COMPLETED)
                .transactionId("txn_12345")
                .currency("USD")
                .completedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void sendPaymentCompletedEvent_Success() {
        // Given
        Payment payment = createTestPayment();

        // When
        paymentEventProducer.sendPaymentCompletedEvent(payment);

        // Then
        verify(kafkaTemplate).send(eq("payment-events"), any(PaymentCompletedEvent.class));
    }

    @Test
    void sendPaymentFailedEvent_Success() {
        // Given
        Payment payment = createTestPayment();
        payment.setStatus(PaymentStatus.FAILED);
        String failureReason = "Insufficient funds";

        // When
        paymentEventProducer.sendPaymentFailedEvent(payment, failureReason);

        // Then
        verify(kafkaTemplate).send(eq("payment-events"), any(PaymentFailedEvent.class));
    }
}