package com.shopping.payment.controller.dto;

import com.shopping.payment.model.PaymentStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UpdatePaymentStatusRequest {

    @NotNull(message = "Status is required")
    private PaymentStatus status;

    private String transactionId;
}