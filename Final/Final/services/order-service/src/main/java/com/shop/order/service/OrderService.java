package com.shop.order.service;

import com.shop.order.model.OrderStatus;
import com.shop.order.model.dto.CreateOrderRequest;
import com.shop.order.model.dto.OrderResponse;
import com.shop.order.model.dto.UpdateOrderRequest;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);
    OrderResponse cancelOrder(String orderId);
    OrderResponse updateOrder(String orderId, UpdateOrderRequest request);
    OrderResponse getOrder(String orderId);
    List<OrderResponse> getOrdersByUser(String userId);

    OrderResponse updateOrderStatus(String orderId, OrderStatus status);

    OrderResponse updateOrderStatusFromString(String orderId, String status);
}