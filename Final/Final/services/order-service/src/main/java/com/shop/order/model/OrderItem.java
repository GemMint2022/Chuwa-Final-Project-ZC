package com.shop.order.model;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

import java.math.BigDecimal;

@Data
@UserDefinedType("order_item")
public class OrderItem {
    private String itemId;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private String imageUrl;

    public OrderItem() {
    }

    public OrderItem(String itemId, String name, BigDecimal price, Integer quantity, String imageUrl) {
        this.itemId = itemId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }
}