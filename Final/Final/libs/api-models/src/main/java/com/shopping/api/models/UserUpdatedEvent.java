package com.shopping.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatedEvent {
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private Long userId;
    private String email;
    private String username;
    private String billingAddress;
    private String shippingAddress;
    private String paymentMethod;
}