package com.shopping.payment.event.producer;

import com.shopping.payment.event.producer.event.PaymentCompletedEvent;
import com.shopping.payment.event.producer.event.PaymentFailedEvent;
import com.shopping.payment.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String PAYMENT_EVENTS_TOPIC = "payment-events";

    public void sendPaymentCompletedEvent(Payment payment) {
        PaymentCompletedEvent event = new PaymentCompletedEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setTimestamp(LocalDateTime.now());
        event.setPaymentId(payment.getPaymentId());
        event.setOrderId(payment.getOrderId());
        event.setUserId(payment.getUserId());
        event.setAmount(payment.getAmount());
        event.setStatus(payment.getStatus().name());
        event.setTransactionId(payment.getTransactionId());
        event.setCompletedAt(payment.getCompletedAt());

        kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, event);
        log.info("Sent PaymentCompletedEvent for payment: {}", payment.getPaymentId());
    }

    public void sendPaymentFailedEvent(Payment payment, String failureReason) {
        PaymentFailedEvent event = new PaymentFailedEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setTimestamp(LocalDateTime.now());
        event.setPaymentId(payment.getPaymentId());
        event.setOrderId(payment.getOrderId());
        event.setUserId(payment.getUserId());
        event.setAmount(payment.getAmount());
        event.setStatus(payment.getStatus().name());
        event.setFailureReason(failureReason);

        kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, event);
        log.info("Sent PaymentFailedEvent for payment: {}, reason: {}",
                payment.getPaymentId(), failureReason);
    }
}