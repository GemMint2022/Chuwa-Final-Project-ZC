package com.shop.order.service;

import com.shop.order.client.ItemServiceClient;
import com.shop.order.exception.InvalidOrderStateException;
import com.shop.order.exception.OrderNotFoundException;
import com.shop.order.exception.ItemServiceException;
import com.shop.order.kafka.producer.OrderEventProducer;
import com.shop.order.model.Order;
import com.shop.order.model.OrderItem;
import com.shop.order.model.OrderStatus;
import com.shop.order.model.dto.CreateOrderRequest;
import com.shop.order.model.dto.OrderResponse;
import com.shop.order.model.dto.UpdateOrderRequest;
import com.shop.order.repository.OrderByUserRepository;
import com.shop.order.repository.OrderRepository;
import com.shop.order.repository.OrderStatusHistoryRepository;
import com.shop.order.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderByUserRepository orderByUserRepository;

    @Mock
    private OrderStatusHistoryRepository statusHistoryRepository;

    @Mock
    private ItemServiceClient itemServiceClient;

    @Mock
    private OrderEventProducer orderEventProducer;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private CreateOrderRequest createRequest;

    @BeforeEach
    void setUp() {
        // Setup test order
        testOrder = new Order();
        testOrder.setOrderId("ORD-TEST-123");
        testOrder.setUserId("user123");
        testOrder.setStatus(OrderStatus.ORDER_CREATED);
        testOrder.setTotalAmount(new BigDecimal("99.99"));
        testOrder.setCreatedAt(Instant.now());
        testOrder.setUpdatedAt(Instant.now());

        List<OrderItem> items = new ArrayList<>();
        OrderItem item = new OrderItem();
        item.setItemId("item001");
        item.setName("Test Product");
        item.setPrice(new BigDecimal("49.99"));
        item.setQuantity(2);
        items.add(item);
        testOrder.setItems(items);

        // Setup create order request
        createRequest = new CreateOrderRequest();
        createRequest.setUserId("user123");

        CreateOrderRequest.Address address = new CreateOrderRequest.Address();
        address.setStreet("123 Main St");
        address.setCity("Test City");
        address.setState("TS");
        address.setZipCode("12345");
        address.setCountry("Test Country");
        createRequest.setShippingAddress(address);

        CreateOrderRequest.OrderItemRequest itemRequest = new CreateOrderRequest.OrderItemRequest();
        itemRequest.setItemId("item001");
        itemRequest.setQuantity(2);
        createRequest.setItems(List.of(itemRequest));
    }

    @Test
    void createOrder_Success() {
        // Given
        ItemServiceClient.ItemInfo itemInfo = new ItemServiceClient.ItemInfo();
        itemInfo.setItemId("item001");
        itemInfo.setName("Test Product");
        itemInfo.setPrice(new BigDecimal("49.99"));
        itemInfo.setStock(10);
        itemInfo.setImageUrl("/test.jpg");

        when(itemServiceClient.getItemsInfo(anyList())).thenReturn(List.of(itemInfo));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        OrderResponse result = orderService.createOrder(createRequest);

        // Then
        assertNotNull(result);
        assertEquals("user123", result.getUserId());
        assertEquals(OrderStatus.ORDER_CREATED, result.getStatus());
        assertNotNull(result.getOrderId());

        verify(itemServiceClient, times(1)).getItemsInfo(anyList());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderEventProducer, times(1)).sendOrderCreatedEvent(any(Order.class));
    }

    @Test
    void getOrder_OrderExists_ReturnsOrder() {
        // Given
        when(orderRepository.findById("ORD-TEST-123")).thenReturn(Optional.of(testOrder));

        // When
        OrderResponse result = orderService.getOrder("ORD-TEST-123");

        // Then
        assertNotNull(result);
        assertEquals("ORD-TEST-123", result.getOrderId());
        assertEquals("user123", result.getUserId());
        verify(orderRepository, times(1)).findById("ORD-TEST-123");
    }

    @Test
    void getOrder_OrderNotFound_ThrowsException() {
        // Given
        when(orderRepository.findById("NON_EXISTENT")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OrderNotFoundException.class, () -> {
            orderService.getOrder("NON_EXISTENT");
        });

        verify(orderRepository, times(1)).findById("NON_EXISTENT");
    }

    @Test
    void cancelOrder_Success() {
        // Given
        testOrder.setStatus(OrderStatus.ORDER_CREATED);
        when(orderRepository.findById("ORD-TEST-123")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        OrderResponse result = orderService.cancelOrder("ORD-TEST-123");

        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.ORDER_CANCELED, result.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderEventProducer, times(1)).sendOrderCanceledEvent(any(Order.class));
    }

    @Test
    void cancelOrder_CompletedOrder_ThrowsException() {
        // Given
        testOrder.setStatus(OrderStatus.ORDER_COMPLETED);
        when(orderRepository.findById("ORD-TEST-123")).thenReturn(Optional.of(testOrder));

        // When & Then
        assertThrows(InvalidOrderStateException.class, () -> {
            orderService.cancelOrder("ORD-TEST-123");
        });

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrder_Success() {
        // Given
        when(orderRepository.findById("ORD-TEST-123")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        UpdateOrderRequest updateRequest = new UpdateOrderRequest();
        Map<String, String> shippingAddress = new HashMap<>();
        shippingAddress.put("street", "456 New St");
        shippingAddress.put("city", "New City");
        updateRequest.setShippingAddress(shippingAddress);

        // When
        OrderResponse result = orderService.updateOrder("ORD-TEST-123", updateRequest);

        // Then
        assertNotNull(result);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void getOrdersByUser_Success() {
        // Given
        when(orderRepository.findByUserId("user123")).thenReturn(List.of(testOrder));

        // When
        List<OrderResponse> results = orderService.getOrdersByUser("user123");

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("user123", results.get(0).getUserId());
        verify(orderRepository, times(1)).findByUserId("user123");
    }

    @Test
    void updateOrderStatus_WithEnum_Success() {
        // Given
        when(orderRepository.findById("ORD-TEST-123")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        OrderResponse result = orderService.updateOrderStatus("ORD-TEST-123", OrderStatus.ORDER_PAID);

        // Then
        assertNotNull(result);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderEventProducer, times(1)).sendOrderPaidEvent(any(Order.class));
    }

    @Test
    void updateOrderStatusFromString_WithValidString_Success() {
        // Given
        when(orderRepository.findById("ORD-TEST-123")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        OrderResponse result = orderService.updateOrderStatusFromString("ORD-TEST-123", "ORDER_PAID");

        // Then
        assertNotNull(result);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderEventProducer, times(1)).sendOrderPaidEvent(any(Order.class));
    }

    @Test
    void updateOrderStatusFromString_WithInvalidString_ThrowsException() {
        // Given
        when(orderRepository.findById("ORD-TEST-123")).thenReturn(Optional.of(testOrder));

        // When & Then
        assertThrows(InvalidOrderStateException.class, () -> {
            orderService.updateOrderStatusFromString("ORD-TEST-123", "INVALID_STATUS");
        });

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_InvalidTransition_ThrowsException() {
        // Given
        testOrder.setStatus(OrderStatus.ORDER_COMPLETED);
        when(orderRepository.findById("ORD-TEST-123")).thenReturn(Optional.of(testOrder));

        // When & Then
        assertThrows(InvalidOrderStateException.class, () -> {
            orderService.updateOrderStatus("ORD-TEST-123", OrderStatus.ORDER_CREATED);
        });

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrder_AlreadyCanceled_ReturnsOrder() {
        // Given
        testOrder.setStatus(OrderStatus.ORDER_CANCELED);
        when(orderRepository.findById("ORD-TEST-123")).thenReturn(Optional.of(testOrder));

        // When
        OrderResponse result = orderService.cancelOrder("ORD-TEST-123");

        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.ORDER_CANCELED, result.getStatus());
        verify(orderRepository, never()).save(any(Order.class));
        verify(orderEventProducer, never()).sendOrderCanceledEvent(any(Order.class));
    }

    @Test
    void updateOrder_EmptyRequest_DoesNothing() {
        // Given
        when(orderRepository.findById("ORD-TEST-123")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        UpdateOrderRequest emptyRequest = new UpdateOrderRequest();

        // When
        OrderResponse result = orderService.updateOrder("ORD-TEST-123", emptyRequest);

        // Then
        assertNotNull(result);
        verify(orderRepository, times(1)).save(any(Order.class));
    }
}