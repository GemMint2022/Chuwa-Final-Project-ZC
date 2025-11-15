package com.shop.order.kafka.consumer;

import com.shop.order.model.OrderStatus;
import com.shop.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final OrderService orderService;

    /**
     * 监听支付成功事件
     */
    @KafkaListener(topics = "${app.kafka.topics.payment-success:payment-success-topic}")
    public void handlePaymentSuccessEvent(Map<String, Object> paymentEvent) {
        try {
            log.info("Received payment success event: {}", paymentEvent);

            String orderId = (String) paymentEvent.get("orderId");
            String paymentId = (String) paymentEvent.get("paymentId");
            Double amount = (Double) paymentEvent.get("amount");

            if (orderId == null) {
                log.warn("Payment success event missing orderId: {}", paymentEvent);
                return;
            }

            // 更新订单状态为已支付
            orderService.updateOrderStatus(orderId, OrderStatus.ORDER_PAID);
            log.info("Successfully updated order {} status to PAID after payment success", orderId);

        } catch (Exception e) {
            log.error("Error processing payment success event: {}", paymentEvent, e);
            // 这里可以添加重试逻辑或者死信队列处理
        }
    }

    /**
     * 监听支付失败事件
     */
    @KafkaListener(topics = "${app.kafka.topics.payment-failed:payment-failed-topic}")
    public void handlePaymentFailedEvent(Map<String, Object> paymentEvent) {
        try {
            log.info("Received payment failed event: {}", paymentEvent);

            String orderId = (String) paymentEvent.get("orderId");
            String reason = (String) paymentEvent.get("reason");

            if (orderId == null) {
                log.warn("Payment failed event missing orderId: {}", paymentEvent);
                return;
            }

            // 根据业务需求处理支付失败，比如标记订单需要重新支付等
            log.warn("Payment failed for order {}: {}", orderId, reason);

        } catch (Exception e) {
            log.error("Error processing payment failed event: {}", paymentEvent, e);
        }
    }
}