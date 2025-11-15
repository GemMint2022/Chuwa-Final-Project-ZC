package com.shop.order.model.dto;

import lombok.Data;

import javax.validation.Valid;
import java.util.Map;

@Data
public class UpdateOrderRequest {


    private Map<@Valid String, String> shippingAddress;


    private Map<String, String> paymentInfo;


    private String notes;


    public boolean isShippingAddressValid() {
        if (shippingAddress == null) return true; // 不更新地址时返回true

        return shippingAddress.containsKey("street") &&
                shippingAddress.containsKey("city") &&
                shippingAddress.containsKey("state") &&
                shippingAddress.containsKey("zipCode") &&
                shippingAddress.containsKey("country");
    }


    public boolean isEmpty() {
        return (shippingAddress == null || shippingAddress.isEmpty()) &&
                (paymentInfo == null || paymentInfo.isEmpty()) &&
                (notes == null || notes.trim().isEmpty());
    }

    public String getValidationErrors() {
        if (shippingAddress != null && !shippingAddress.isEmpty() && !isShippingAddressValid()) {
            return "Shipping address must contain street, city, state, zipCode and country";
        }
        return null;
    }
}