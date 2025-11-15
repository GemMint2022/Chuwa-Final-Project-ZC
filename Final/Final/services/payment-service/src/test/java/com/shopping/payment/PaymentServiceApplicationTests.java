package com.shopping.payment;

import com.shopping.payment.config.TestKafkaConfig;
import com.shopping.payment.event.producer.PaymentEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestKafkaConfig.class)  // 导入测试配置
class PaymentServiceApplicationTests {

    @MockBean
    private PaymentEventProducer paymentEventProducer;  // 只需要模拟业务Bean

    @Test
    void contextLoads() {
        // 验证应用上下文加载成功
    }
}