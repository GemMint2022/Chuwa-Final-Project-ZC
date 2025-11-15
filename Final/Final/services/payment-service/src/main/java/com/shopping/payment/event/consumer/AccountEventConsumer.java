package com.shopping.payment.event.consumer;

import com.shopping.api.models.UserCreatedEvent;
import com.shopping.api.models.UserUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountEventConsumer {

    @KafkaListener(topics = "user-events", groupId = "payment-service")
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        log.info("Received UserCreatedEvent for user: {}", event.getUserId());
        // In a real scenario, you might want to:
        // - Initialize user payment preferences
        // - Create a payment profile for the new user
        // - Set up default payment methods
        log.info("User created event processed for user: {}", event.getUserId());
    }

    @KafkaListener(topics = "user-events", groupId = "payment-service")
    public void handleUserUpdatedEvent(UserUpdatedEvent event) {
        log.info("Received UserUpdatedEvent for user: {}", event.getUserId());
        // In a real scenario, you might want to:
        // - Update user payment preferences
        // - Sync billing address changes
        // - Update payment method information
        log.info("User updated event processed for user: {}", event.getUserId());
    }
}