package com.shop.order.service.impl;

import com.shop.order.client.ItemServiceClient;
import com.shop.order.exception.InvalidOrderStateException;
import com.shop.order.exception.ItemServiceException;
import com.shop.order.exception.OrderNotFoundException;
import com.shop.order.kafka.producer.OrderEventProducer;
import com.shop.order.model.Order;
import com.shop.order.model.OrderByUser;
import com.shop.order.model.OrderItem;
import com.shop.order.model.OrderStatus;
import com.shop.order.model.OrderStatusHistory;
import com.shop.order.model.dto.CreateOrderRequest;
import com.shop.order.model.dto.OrderResponse;
import com.shop.order.model.dto.UpdateOrderRequest;
import com.shop.order.repository.OrderByUserRepository;
import com.shop.order.repository.OrderRepository;
import com.shop.order.repository.OrderStatusHistoryRepository;
import com.shop.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderByUserRepository orderByUserRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final ItemServiceClient itemServiceClient;
    private final OrderEventProducer orderEventProducer;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for user: {}", request.getUserId());

        try {
            List<String> itemIds = request.getItems().stream()
                    .map(CreateOrderRequest.OrderItemRequest::getItemId)
                    .collect(Collectors.toList());

            List<ItemServiceClient.ItemInfo> itemInfos = itemServiceClient.getItemsInfo(itemIds);

            List<OrderItem> orderItems = createOrderItems(request.getItems(), itemInfos);
            BigDecimal totalAmount = calculateTotalAmount(orderItems);

            Order order = new Order();
            order.setOrderId(generateOrderId());
            order.setUserId(request.getUserId());
            order.setStatus(OrderStatus.ORDER_CREATED);
            order.setTotalAmount(totalAmount);
            order.setItems(orderItems);
            order.setCreatedAt(Instant.now());
            order.setUpdatedAt(Instant.now());
            order.setShippingAddress(convertAddressToMap(request.getShippingAddress()));

            orderRepository.save(order);
            saveOrderByUser(order);
            saveStatusHistory(order.getOrderId(), OrderStatus.ORDER_CREATED, "Order created successfully");

            orderEventProducer.sendOrderCreatedEvent(order);

            log.info("Order created successfully: {}", order.getOrderId());
            return convertToResponse(order);

        } catch (ItemServiceException e) {
            log.error("Failed to create order due to item service error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while creating order: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create order", e);
        }
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(String orderId) {
        log.info("Cancelling order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        if (order.getStatus() == OrderStatus.ORDER_COMPLETED) {
            throw new InvalidOrderStateException("Cannot cancel completed order");
        }

        if (order.getStatus() == OrderStatus.ORDER_CANCELED) {
            log.warn("Order {} is already canceled", orderId);
            return convertToResponse(order);
        }

        order.setStatus(OrderStatus.ORDER_CANCELED);
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);

        updateOrderStatusInUserTable(order);
        saveStatusHistory(orderId, OrderStatus.ORDER_CANCELED, "Order canceled by user");

        orderEventProducer.sendOrderCanceledEvent(order);

        log.info("Order canceled successfully: {}", orderId);
        return convertToResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(String orderId, UpdateOrderRequest request) {
        log.info("Updating order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        if (order.getStatus() == OrderStatus.ORDER_COMPLETED || order.getStatus() == OrderStatus.ORDER_CANCELED) {
            throw new InvalidOrderStateException("Cannot update order in " + order.getStatus() + " state");
        }

        if (request.getShippingAddress() != null && !request.getShippingAddress().isEmpty()) {
            order.setShippingAddress(request.getShippingAddress());
        }
        if (request.getPaymentInfo() != null && !request.getPaymentInfo().isEmpty()) {
            order.setPaymentInfo(request.getPaymentInfo());
        }

        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);

        saveStatusHistory(orderId, order.getStatus(), "Order details updated");

        log.info("Order updated successfully: {}", orderId);
        return convertToResponse(order);
    }

    @Override
    public OrderResponse getOrder(String orderId) {
        log.debug("Fetching order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        return convertToResponse(order);
    }

    @Override
    public List<OrderResponse> getOrdersByUser(String userId) {
        log.debug("Fetching orders for user: {}", userId);

        List<Order> orders = orderRepository.findByUserId(userId);

        if (orders.isEmpty()) {
            log.info("No orders found for user: {}", userId);
        }

        return orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(String orderId, OrderStatus status) {
        log.info("Updating order status: {} to {}", orderId, status);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        validateStatusTransition(order.getStatus(), status);

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(status);
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);

        updateOrderStatusInUserTable(order);
        saveStatusHistory(orderId, status,
                String.format("Order status changed from %s to %s", previousStatus, status));

        publishStatusChangeEvent(order, previousStatus);

        log.info("Order status updated successfully: {} -> {}", orderId, status);
        return convertToResponse(order);
    }

    @Override
    public OrderResponse updateOrderStatusFromString(String orderId, String statusStr) {
        try {
            OrderStatus status = OrderStatus.valueOf(statusStr.toUpperCase());
            return updateOrderStatus(orderId, status);
        } catch (IllegalArgumentException e) {
            throw new InvalidOrderStateException("Invalid order status: " + statusStr);
        }
    }

    private List<OrderItem> createOrderItems(List<CreateOrderRequest.OrderItemRequest> itemRequests,
                                             List<ItemServiceClient.ItemInfo> itemInfos) {
        return itemRequests.stream().map(itemRequest -> {
            ItemServiceClient.ItemInfo itemInfo = itemInfos.stream()
                    .filter(info -> info.getItemId().equals(itemRequest.getItemId()))
                    .findFirst()
                    .orElseThrow(() -> new ItemServiceException("Item not found: " + itemRequest.getItemId()));

            // 检查库存
            if (itemInfo.getStock() < itemRequest.getQuantity()) {
                throw new ItemServiceException(
                        String.format("Insufficient stock for item %s. Available: %d, Requested: %d",
                                itemInfo.getItemId(), itemInfo.getStock(), itemRequest.getQuantity())
                );
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setItemId(itemInfo.getItemId());
            orderItem.setName(itemInfo.getName());
            orderItem.setPrice(itemInfo.getPrice());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setImageUrl(itemInfo.getImageUrl());
            return orderItem;
        }).collect(Collectors.toList());
    }

    private BigDecimal calculateTotalAmount(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateOrderId() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Map<String, String> convertAddressToMap(CreateOrderRequest.Address address) {
        return Map.of(
                "street", address.getStreet(),
                "city", address.getCity(),
                "state", address.getState(),
                "zipCode", address.getZipCode(),
                "country", address.getCountry()
        );
    }

    private void saveOrderByUser(Order order) {
        OrderByUser orderByUser = new OrderByUser();
        OrderByUser.OrderByUserKey key = new OrderByUser.OrderByUserKey();
        key.setUserId(order.getUserId());
        key.setOrderId(order.getOrderId());

        orderByUser.setKey(key);
        orderByUser.setStatus(order.getStatus().name());
        orderByUser.setTotalAmount(order.getTotalAmount());
        orderByUser.setCreatedAt(order.getCreatedAt());

        orderByUserRepository.save(orderByUser);
    }

    private void updateOrderStatusInUserTable(Order order) {
        List<OrderByUser> userOrders = orderByUserRepository.findByKeyUserId(order.getUserId());
        userOrders.stream()
                .filter(userOrder -> userOrder.getKey().getOrderId().equals(order.getOrderId()))
                .findFirst()
                .ifPresent(userOrder -> {
                    userOrder.setStatus(order.getStatus().name());
                    orderByUserRepository.save(userOrder);
                });
    }


    private void saveStatusHistory(String orderId, OrderStatus status, String notes) {
        OrderStatusHistory history = new OrderStatusHistory();
        OrderStatusHistory.OrderStatusHistoryKey key = new OrderStatusHistory.OrderStatusHistoryKey();
        key.setOrderId(orderId);
        key.setUpdatedAt(Instant.now());

        history.setKey(key);
        history.setNotes(notes);

        statusHistoryRepository.save(history);
    }

    private void validateStatusTransition(OrderStatus from, OrderStatus to) {
        switch (from) {
            case ORDER_CREATED:
                if (to != OrderStatus.ORDER_PAID && to != OrderStatus.ORDER_CANCELED) {
                    throw new InvalidOrderStateException(
                            String.format("Invalid status transition: %s -> %s", from, to)
                    );
                }
                break;
            case ORDER_PAID:
                if (to != OrderStatus.ORDER_COMPLETED && to != OrderStatus.ORDER_CANCELED) {
                    throw new InvalidOrderStateException(
                            String.format("Invalid status transition: %s -> %s", from, to)
                    );
                }
                break;
            case ORDER_COMPLETED:
                throw new InvalidOrderStateException("Cannot change status from COMPLETED");
            case ORDER_CANCELED:
                throw new InvalidOrderStateException("Cannot change status from CANCELED");
        }
    }

    private void publishStatusChangeEvent(Order order, OrderStatus previousStatus) {
        switch (order.getStatus()) {
            case ORDER_PAID:
                orderEventProducer.sendOrderPaidEvent(order);
                break;
            case ORDER_COMPLETED:
                orderEventProducer.sendOrderCompletedEvent(order);
                break;
            case ORDER_CANCELED:
                orderEventProducer.sendOrderCanceledEvent(order);
                break;
        }
    }

    private OrderResponse convertToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());
        response.setUserId(order.getUserId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setShippingAddress(order.getShippingAddress());
        response.setPaymentInfo(order.getPaymentInfo());

        if (order.getItems() != null) {
            List<OrderResponse.OrderItemResponse> itemResponses = order.getItems().stream()
                    .map(this::convertOrderItemToResponse)
                    .collect(Collectors.toList());
            response.setItems(itemResponses);
        }

        return response;
    }

    private OrderResponse.OrderItemResponse convertOrderItemToResponse(OrderItem orderItem) {
        OrderResponse.OrderItemResponse response = new OrderResponse.OrderItemResponse();
        response.setItemId(orderItem.getItemId());
        response.setName(orderItem.getName());
        response.setPrice(orderItem.getPrice());
        response.setQuantity(orderItem.getQuantity());
        response.setImageUrl(orderItem.getImageUrl());
        return response;
    }
}