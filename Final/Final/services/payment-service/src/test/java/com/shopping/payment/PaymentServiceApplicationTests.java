package com.shopping.payment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import com.shopping.payment.config.TestSecurityConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)  // 导入测试安全配置
class PaymentServiceApplicationTests {

    @Test
    void contextLoads() {
        // 验证应用上下文加载成功
    }
}