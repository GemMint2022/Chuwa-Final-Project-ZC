package com.shopping.payment.event.producer.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentFailedEvent extends PaymentEvent {
    private String failureReason;

    public PaymentFailedEvent() {
        setEventType("PAYMENT_FAILED");
    }
}