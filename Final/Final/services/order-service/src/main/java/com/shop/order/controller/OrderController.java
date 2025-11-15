package com.shop.order.controller;

import com.shop.order.model.OrderStatus;
import com.shopping.common.ApiResponse;
import com.shop.order.model.dto.CreateOrderRequest;
import com.shop.order.model.dto.OrderResponse;
import com.shop.order.model.dto.UpdateOrderRequest;
import com.shop.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Creating order for user: {}", request.getUserId());
        OrderResponse order = orderService.createOrder(request);
        return ResponseEntity.ok(ApiResponse.success("Order created successfully", order));
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable String orderId) {
        log.info("Cancelling order: {}", orderId);
        OrderResponse order = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", order));
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(
            @PathVariable String orderId,
            @Valid @RequestBody UpdateOrderRequest request) {
        log.info("Updating order: {}", orderId);
        OrderResponse order = orderService.updateOrder(orderId, request);
        return ResponseEntity.ok(ApiResponse.success("Order updated successfully", order));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable String orderId) {
        log.info("Fetching order: {}", orderId);
        OrderResponse order = orderService.getOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByUser(@PathVariable String userId) {
        log.info("Fetching orders for user: {}", userId);
        List<OrderResponse> orders = orderService.getOrdersByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PutMapping("/{orderId}/status/{status}")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable String orderId,
            @PathVariable String status) {
        log.info("Updating order status: {} to {}", orderId, status);

        OrderResponse order = orderService.updateOrderStatusFromString(orderId, status);
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", order));
    }
}