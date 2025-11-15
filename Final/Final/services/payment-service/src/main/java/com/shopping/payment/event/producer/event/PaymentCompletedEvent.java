package com.shopping.payment.event.producer.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentCompletedEvent extends PaymentEvent {
    private String transactionId;
    private LocalDateTime completedAt;

    public PaymentCompletedEvent() {
        setEventType("PAYMENT_COMPLETED");
    }
}