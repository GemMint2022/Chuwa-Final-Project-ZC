package com.shop.order.kafka.model;

import com.shop.order.model.Order;
import com.shop.order.model.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Data
public class OrderEvent {
    private String eventId;
    private String eventType; // ORDER_CREATED, ORDER_CANCELED, ORDER_PAID, ORDER_COMPLETED
    private String orderId;
    private String userId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private Instant eventTime;
    private Map<String, Object> payload;


    public static OrderEvent createOrderCreatedEvent(Order order) {
        OrderEvent event = new OrderEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("ORDER_CREATED");
        event.setOrderId(order.getOrderId());
        event.setUserId(order.getUserId());
        event.setStatus(order.getStatus());
        event.setTotalAmount(order.getTotalAmount());
        event.setEventTime(Instant.now());
        event.setPayload(Map.of(
                "items", order.getItems(),
                "shippingAddress", order.getShippingAddress()
        ));
        return event;
    }

    public static OrderEvent createOrderCanceledEvent(Order order) {
        OrderEvent event = new OrderEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("ORDER_CANCELED");
        event.setOrderId(order.getOrderId());
        event.setUserId(order.getUserId());
        event.setStatus(order.getStatus());
        event.setTotalAmount(order.getTotalAmount());
        event.setEventTime(Instant.now());
        return event;
    }

    public static OrderEvent createOrderPaidEvent(Order order) {
        OrderEvent event = new OrderEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("ORDER_PAID");
        event.setOrderId(order.getOrderId());
        event.setUserId(order.getUserId());
        event.setStatus(order.getStatus());
        event.setTotalAmount(order.getTotalAmount());
        event.setEventTime(Instant.now());
        return event;
    }

    public static OrderEvent createOrderCompletedEvent(Order order) {
        OrderEvent event = new OrderEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("ORDER_COMPLETED");
        event.setOrderId(order.getOrderId());
        event.setUserId(order.getUserId());
        event.setStatus(order.getStatus());
        event.setTotalAmount(order.getTotalAmount());
        event.setEventTime(Instant.now());
        return event;
    }
}