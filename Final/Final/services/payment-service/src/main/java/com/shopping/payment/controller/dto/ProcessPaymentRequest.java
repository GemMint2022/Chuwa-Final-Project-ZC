package com.shopping.payment.controller.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ProcessPaymentRequest {

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;
}