package com.shopping.payment.event.producer.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private String paymentId;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String status;
}