package com.shop.order.kafka.producer;

import com.shop.order.kafka.model.OrderEvent;
import com.shop.order.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.order-created:order-created-topic}")
    private String orderCreatedTopic;

    @Value("${app.kafka.topics.order-canceled:order-canceled-topic}")
    private String orderCanceledTopic;

    @Value("${app.kafka.topics.order-paid:order-paid-topic}")
    private String orderPaidTopic;

    @Value("${app.kafka.topics.order-completed:order-completed-topic}")
    private String orderCompletedTopic;


    public void sendOrderCreatedEvent(Order order) {
        OrderEvent event = OrderEvent.createOrderCreatedEvent(order);
        sendEvent(orderCreatedTopic, order.getOrderId(), event, "ORDER_CREATED");
    }


    public void sendOrderCanceledEvent(Order order) {
        OrderEvent event = OrderEvent.createOrderCanceledEvent(order);
        sendEvent(orderCanceledTopic, order.getOrderId(), event, "ORDER_CANCELED");
    }


    public void sendOrderPaidEvent(Order order) {
        OrderEvent event = OrderEvent.createOrderPaidEvent(order);
        sendEvent(orderPaidTopic, order.getOrderId(), event, "ORDER_PAID");
    }


    public void sendOrderCompletedEvent(Order order) {
        OrderEvent event = OrderEvent.createOrderCompletedEvent(order);
        sendEvent(orderCompletedTopic, order.getOrderId(), event, "ORDER_COMPLETED");
    }


    private void sendEvent(String topic, String key, OrderEvent event, String eventType) {
        try {
            ListenableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(topic, key, event);

            future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
                @Override
                public void onSuccess(SendResult<String, Object> result) {
                    log.info("Successfully sent {} event for order {} to topic {}, partition {}, offset {}",
                            eventType, key, topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }

                @Override
                public void onFailure(Throwable ex) {
                    log.error("Failed to send {} event for order {} to topic {}: {}",
                            eventType, key, topic, ex.getMessage(), ex);
                }
            });

        } catch (Exception e) {
            log.error("Exception while sending {} event for order {} to topic {}: {}",
                    eventType, key, topic, e.getMessage(), e);
        }
    }


    public boolean sendOrderCreatedEventSync(Order order) {
        OrderEvent event = OrderEvent.createOrderCreatedEvent(order);
        try {
            SendResult<String, Object> result = kafkaTemplate.send(orderCreatedTopic, order.getOrderId(), event)
                    .get();
            log.info("Sync sent ORDER_CREATED event for order {} to topic {}", order.getOrderId(), orderCreatedTopic);
            return true;
        } catch (Exception e) {
            log.error("Failed to sync send ORDER_CREATED event for order {}: {}", order.getOrderId(), e.getMessage());
            return false;
        }
    }
}