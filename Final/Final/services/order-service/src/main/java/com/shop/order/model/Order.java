package com.shop.order.model;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Table("orders")
public class Order {
    @PrimaryKey
    private String orderId;

    private String userId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private Instant updatedAt;

    @Column("items")
    private List<OrderItem> items;

    @Column("shipping_address")
    private Map<String, String> shippingAddress;

    @Column("payment_info")
    private Map<String, String> paymentInfo;

    public Order() {
    }
}