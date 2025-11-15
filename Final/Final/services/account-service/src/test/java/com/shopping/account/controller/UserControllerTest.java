package com.shopping.account.controller;

import com.shopping.account.config.TestSecurityConfig;
import com.shopping.account.dto.UpdateUserRequest;
import com.shopping.account.dto.UserResponse;
import com.shopping.account.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopping.common.security.JwtTokenUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponse createUserResponse() {
        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setEmail("test@example.com");
        response.setUsername("testuser");
        response.setShippingAddress("123 Test St");
        response.setBillingAddress("123 Test St");
        response.setEnabled(true);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getUser_ShouldReturnUser_WhenAuthorized() throws Exception {
        // Arrange
        UserResponse response = createUserResponse();
        when(userService.getUserById(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void getUserByEmail_ShouldReturnUser_WhenEmailExists() throws Exception {
        // Arrange
        UserResponse response = createUserResponse();
        when(userService.getUserByEmail("test@example.com")).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/users/email/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateUser_ShouldSuccess_WhenValidRequest() throws Exception {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("updateduser");
        request.setShippingAddress("456 Updated St");

        UserResponse response = createUserResponse();
        response.setUsername("updateduser");
        response.setShippingAddress("456 Updated St");

        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void deleteUser_ShouldSuccess_WhenAuthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/users/1")
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}