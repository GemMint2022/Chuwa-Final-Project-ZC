package com.shop.order.model.dto;

import com.shop.order.model.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
public class OrderResponse {
    private String orderId;
    private String userId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private Instant updatedAt;
    private List<OrderItemResponse> items;
    private Map<String, String> shippingAddress;
    private Map<String, String> paymentInfo;

    public Integer getTotalItems() {
        return items != null ?
                items.stream().mapToInt(OrderItemResponse::getQuantity).sum() : 0;
    }

    public String getFormattedTotalAmount() {
        return totalAmount != null ? "$" + totalAmount.setScale(2).toString() : "$0.00";
    }

    public String getStatusDisplayName() {
        if (status == null) return "Unknown";

        switch (status) {
            case ORDER_CREATED:
                return "Created";
            case ORDER_PAID:
                return "Paid";
            case ORDER_COMPLETED:
                return "Completed";
            case ORDER_CANCELED:
                return "Canceled";
            default:
                return status.name();
        }
    }

    @Data
    public static class OrderItemResponse {
        private String itemId;
        private String name;
        private BigDecimal price;
        private Integer quantity;
        private String imageUrl;

        public BigDecimal getItemTotal() {
            return price != null && quantity != null ?
                    price.multiply(BigDecimal.valueOf(quantity)) : BigDecimal.ZERO;
        }

        public String getFormattedPrice() {
            return price != null ? "$" + price.setScale(2).toString() : "$0.00";
        }

        public String getFormattedItemTotal() {
            return "$" + getItemTotal().setScale(2).toString();
        }
    }

    public static OrderResponse from(com.shop.order.model.Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());
        response.setUserId(order.getUserId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setShippingAddress(order.getShippingAddress());
        response.setPaymentInfo(order.getPaymentInfo());
        return response;
    }
}