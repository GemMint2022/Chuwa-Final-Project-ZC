package com.shop.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.order.model.OrderStatus;
import com.shop.order.model.dto.CreateOrderRequest;
import com.shop.order.model.dto.OrderResponse;
import com.shop.order.model.dto.UpdateOrderRequest;
import com.shop.order.repository.OrderByUserRepository;
import com.shop.order.repository.OrderRepository;
import com.shop.order.repository.OrderStatusHistoryRepository;
import com.shop.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@TestPropertySource(properties = {
        "spring.data.cassandra.keyspace-name=test_keyspace",
        "spring.data.cassandra.schema-action=none",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration"
})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderRepository orderRepository;
    @MockBean
    private OrderByUserRepository orderByUserRepository;
    @MockBean
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    private OrderResponse createMockOrderResponse() {
        OrderResponse response = new OrderResponse();
        response.setOrderId("ORD-TEST-123");
        response.setUserId("user123");
        response.setStatus(OrderStatus.ORDER_CREATED);
        response.setTotalAmount(new BigDecimal("99.99"));
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());
        response.setShippingAddress(Map.of(
                "street", "123 Main St",
                "city", "Test City"
        ));
        return response;
    }

    @Test
    void createOrder_ValidRequest_ReturnsSuccess() throws Exception {
        // Given
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId("user123");

        CreateOrderRequest.Address address = new CreateOrderRequest.Address();
        address.setStreet("123 Main St");
        address.setCity("Test City");
        address.setState("TS");
        address.setZipCode("12345");
        address.setCountry("Test Country");
        request.setShippingAddress(address);

        CreateOrderRequest.OrderItemRequest itemRequest = new CreateOrderRequest.OrderItemRequest();
        itemRequest.setItemId("item001");
        itemRequest.setQuantity(2);
        request.setItems(List.of(itemRequest));

        OrderResponse mockResponse = createMockOrderResponse();
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value("ORD-TEST-123"));
    }

    @Test
    void getOrder_OrderExists_ReturnsOrder() throws Exception {
        // Given
        OrderResponse mockResponse = createMockOrderResponse();
        when(orderService.getOrder("ORD-TEST-123")).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/orders/ORD-TEST-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value("ORD-TEST-123"))
                .andExpect(jsonPath("$.data.userId").value("user123"));
    }

    @Test
    void cancelOrder_Success() throws Exception {
        // Given
        OrderResponse mockResponse = createMockOrderResponse();
        mockResponse.setStatus(OrderStatus.ORDER_CANCELED);
        when(orderService.cancelOrder("ORD-TEST-123")).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(put("/api/orders/ORD-TEST-123/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order cancelled successfully"));
    }

    @Test
    void updateOrder_Success() throws Exception {
        // Given
        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setShippingAddress(Map.of("street", "456 New St"));

        OrderResponse mockResponse = createMockOrderResponse();
        when(orderService.updateOrder(eq("ORD-TEST-123"), any(UpdateOrderRequest.class))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(put("/api/orders/ORD-TEST-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order updated successfully"));
    }

    @Test
    void getOrdersByUser_Success() throws Exception {
        // Given
        OrderResponse mockResponse = createMockOrderResponse();
        when(orderService.getOrdersByUser("user123")).thenReturn(List.of(mockResponse));

        // When & Then
        mockMvc.perform(get("/api/orders/user/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].userId").value("user123"));
    }

    @Test
    void updateOrderStatus_Success() throws Exception {
        // Given
        OrderResponse mockResponse = createMockOrderResponse();
        mockResponse.setStatus(OrderStatus.ORDER_PAID);
        when(orderService.updateOrderStatusFromString(eq("ORD-TEST-123"), eq("ORDER_PAID"))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(put("/api/orders/ORD-TEST-123/status/ORDER_PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order status updated successfully"));
    }
}