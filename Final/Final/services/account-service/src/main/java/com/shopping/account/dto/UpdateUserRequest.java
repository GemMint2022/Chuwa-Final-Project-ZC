package com.shopping.account.dto;

import lombok.Data;
import javax.validation.constraints.Size;

@Data
public class UpdateUserRequest {
    @Size(min = 2, max = 50, message = "Username must be between 2 and 50 characters")
    private String username;

    private String shippingAddress;
    private String billingAddress;
    private String paymentMethod;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password; // Optional password update
}