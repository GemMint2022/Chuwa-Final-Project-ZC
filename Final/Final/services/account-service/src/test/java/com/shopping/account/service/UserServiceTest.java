package com.shopping.account.service;

import com.shopping.account.dto.UpdateUserRequest;
import com.shopping.account.dto.UserResponse;
import com.shopping.account.entity.User;
import com.shopping.account.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setShippingAddress("123 Test St");
        user.setBillingAddress("123 Test St");
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        // Arrange
        User user = createUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        UserResponse result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.getUserById(1L));
    }

    @Test
    void updateUser_ShouldUpdateFields_WhenValidRequest() {
        // Arrange
        User existingUser = createUser();
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("updateduser");
        request.setShippingAddress("456 Updated St");
        request.setPassword("newpassword123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsername("updateduser")).thenReturn(false);
        when(passwordEncoder.encode("newpassword123")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserResponse result = userService.updateUser(1L, request);

        // Assert
        assertNotNull(result);
        assertEquals("updateduser", result.getUsername());
        assertEquals("456 Updated St", result.getShippingAddress());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_ShouldThrowException_WhenUsernameTaken() {
        // Arrange
        User existingUser = createUser();
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("takenusername");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsername("takenusername")).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.updateUser(1L, request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_ShouldCallRepository_WhenUserExists() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void getUserByEmail_ShouldReturnUser_WhenEmailExists() {
        // Arrange
        User user = createUser();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Act
        UserResponse result = userService.getUserByEmail("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }
}