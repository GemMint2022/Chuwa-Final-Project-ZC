package com.shopping.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String email;
    private String username;

    public LoginResponse(String token, Long userId, String email, String username) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.username = username;
    }
}