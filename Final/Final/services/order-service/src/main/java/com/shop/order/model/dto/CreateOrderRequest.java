package com.shop.order.model.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;


@Data
public class CreateOrderRequest {
    @NotBlank
    private String userId;

    @NotEmpty
    private List<OrderItemRequest> items;

    @Valid
    private Address shippingAddress;

    @Data
    public static class OrderItemRequest {
        @NotBlank
        private String itemId;
        private Integer quantity;
    }

    @Data
    public static class Address {
        @NotBlank
        private String street;
        @NotBlank
        private String city;
        @NotBlank
        private String state;
        @NotBlank
        private String zipCode;
        @NotBlank
        private String country;
    }
}