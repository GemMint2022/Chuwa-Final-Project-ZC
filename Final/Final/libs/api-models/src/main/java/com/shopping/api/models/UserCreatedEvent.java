package com.shopping.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private Long userId;
    private String email;
    private String username;
}