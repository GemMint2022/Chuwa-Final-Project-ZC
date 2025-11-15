package com.shop.order.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // 生产者配置优化
        configProps.put(ProducerConfig.ACKS_CONFIG, "all"); // 确保消息持久化
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3); // 重试次数
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // 批量大小
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 10); // 等待时间
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432); // 缓冲区大小

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // 创建Kafka主题（如果不存在）
    @Bean
    public NewTopic orderCreatedTopic() {
        return new NewTopic("order-created-topic", 3, (short) 1); // 3个分区，1个副本
    }

    @Bean
    public NewTopic orderCanceledTopic() {
        return new NewTopic("order-canceled-topic", 3, (short) 1);
    }

    @Bean
    public NewTopic orderPaidTopic() {
        return new NewTopic("order-paid-topic", 3, (short) 1);
    }

    @Bean
    public NewTopic orderCompletedTopic() {
        return new NewTopic("order-completed-topic", 3, (short) 1);
    }

    @Bean
    public NewTopic paymentSuccessTopic() {
        return new NewTopic("payment-success-topic", 3, (short) 1);
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return new NewTopic("payment-failed-topic", 3, (short) 1);
    }
}