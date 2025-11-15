package com.shopping.payment.event.consumer;

import com.shopping.api.models.UserCreatedEvent;
import com.shopping.api.models.UserUpdatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class AccountEventConsumerTest {

    @InjectMocks
    private AccountEventConsumer accountEventConsumer;

    @Test
    void handleUserCreatedEvent_Success() {
        // Given
        UserCreatedEvent event = new UserCreatedEvent();
        event.setEventId("event_123");
        event.setEventType("USER_CREATED");
        event.setTimestamp(LocalDateTime.now());
        event.setUserId(5001L);
        event.setEmail("test@example.com");
        event.setUsername("testuser");

        // When & Then - Should not throw any exception
        assertDoesNotThrow(() -> accountEventConsumer.handleUserCreatedEvent(event));
    }

    @Test
    void handleUserUpdatedEvent_Success() {
        // Given
        UserUpdatedEvent event = new UserUpdatedEvent();
        event.setEventId("event_456");
        event.setEventType("USER_UPDATED");
        event.setTimestamp(LocalDateTime.now());
        event.setUserId(5001L);
        event.setEmail("updated@example.com");
        event.setUsername("updateduser");
        event.setBillingAddress("123 Updated St");
        event.setPaymentMethod("CREDIT_CARD");

        // When & Then - Should not throw any exception
        assertDoesNotThrow(() -> accountEventConsumer.handleUserUpdatedEvent(event));
    }
}