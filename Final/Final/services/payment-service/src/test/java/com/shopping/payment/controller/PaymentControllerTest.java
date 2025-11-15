package com.shopping.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopping.common.GlobalExceptionHandler;
import com.shopping.common.security.JwtTokenUtil;
import com.shopping.payment.config.TestSecurityConfig;
import com.shopping.payment.controller.dto.PaymentRequest;
import com.shopping.payment.controller.dto.ProcessPaymentRequest;
import com.shopping.payment.controller.dto.RefundRequest;
import com.shopping.payment.controller.dto.UpdatePaymentStatusRequest;
import com.shopping.payment.model.Payment;
import com.shopping.payment.model.PaymentStatus;
import com.shopping.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})  // 显式导入异常处理器
@TestPropertySource(properties = {
        "spring.security.basic.enabled=false",
        "security.basic.enabled=false"
})
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    private Payment createTestPayment() {
        return Payment.builder()
                .paymentId("pay_test123")
                .orderId(1001L)
                .userId(5001L)
                .amount(new BigDecimal("99.99"))
                .status(PaymentStatus.PENDING)
                .currency("USD")
                .build();
    }

    @BeforeEach
    void setUp() {
        // 设置测试环境
    }

    @Test
    void createPayment_Success() throws Exception {
        // Given
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(1001L);
        request.setAmount(new BigDecimal("99.99"));
        request.setPaymentMethod("CREDIT_CARD");
        request.setCurrency("USD");
        request.setIdempotencyKey("idemp_123");

        Payment payment = createTestPayment();
        when(paymentService.createPayment(any(), any(), any(), any(), any(), any()))
                .thenReturn(payment);

        // When & Then - 添加认证头
        mockMvc.perform(post("/api/payments")
                        .header("X-User-Id", "5001")
                        .header("Authorization", "Bearer mock-token")  // 添加认证头
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Payment created successfully"))
                .andExpect(jsonPath("$.data.paymentId").value("pay_test123"));
    }

    @Test
    void createPayment_InvalidRequest_ReturnsBadRequest() throws Exception {

        PaymentRequest request = new PaymentRequest();
        request.setOrderId(1001L);

        mockMvc.perform(post("/api/payments")
                        .header("X-User-Id", "5001")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getPayment_Success() throws Exception {
        // Given
        Payment payment = createTestPayment();
        when(paymentService.getPaymentById("pay_test123"))
                .thenReturn(payment);

        // When & Then - 添加认证头
        mockMvc.perform(get("/api/payments/pay_test123")
                        .header("Authorization", "Bearer mock-token"))  // 添加认证头
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentId").value("pay_test123"))
                .andExpect(jsonPath("$.data.orderId").value(1001));
    }

    @Test
    void processPayment_Success() throws Exception {
        // Given
        ProcessPaymentRequest request = new ProcessPaymentRequest();
        request.setTransactionId("txn_12345");

        Payment payment = createTestPayment();
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId("txn_12345");

        when(paymentService.processPayment("pay_test123", "txn_12345"))
                .thenReturn(payment);

        // When & Then - 添加认证头
        mockMvc.perform(post("/api/payments/pay_test123/process")
                        .header("Authorization", "Bearer mock-token")  // 添加认证头
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.transactionId").value("txn_12345"));
    }

    @Test
    void cancelPayment_Success() throws Exception {
        // Given
        Payment payment = createTestPayment();
        payment.setStatus(PaymentStatus.CANCELLED);

        when(paymentService.cancelPayment("pay_test123"))
                .thenReturn(payment);

        // When & Then - 添加认证头
        mockMvc.perform(post("/api/payments/pay_test123/cancel")
                        .header("Authorization", "Bearer mock-token"))  // 添加认证头
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    void refundPayment_Success() throws Exception {
        // Given
        RefundRequest request = new RefundRequest();
        request.setAmount(new BigDecimal("50.00"));

        Payment payment = createTestPayment();
        payment.setStatus(PaymentStatus.REFUNDED);

        when(paymentService.refundPayment("pay_test123", new BigDecimal("50.00")))
                .thenReturn(payment);

        // When & Then - 添加认证头
        mockMvc.perform(post("/api/payments/pay_test123/refund")
                        .header("Authorization", "Bearer mock-token")  // 添加认证头
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("REFUNDED"));
    }

    @Test
    void getPaymentsByUser_Success() throws Exception {
        // Given
        Payment payment = createTestPayment();
        when(paymentService.getPaymentsByUserId(5001L))
                .thenReturn(Collections.singletonList(payment));

        // When & Then - 添加认证头
        mockMvc.perform(get("/api/payments/user/5001")
                        .header("Authorization", "Bearer mock-token"))  // 添加认证头
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].paymentId").value("pay_test123"));
    }

    @Test
    void getPaymentsByOrder_Success() throws Exception {
        // Given
        Payment payment = createTestPayment();
        when(paymentService.getPaymentsByOrderId(1001L))
                .thenReturn(Collections.singletonList(payment));

        // When & Then - 添加认证头
        mockMvc.perform(get("/api/payments/order/1001")
                        .header("Authorization", "Bearer mock-token"))  // 添加认证头
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].paymentId").value("pay_test123"));
    }

    @Test
    void updatePaymentStatus_Success() throws Exception {
        // Given
        UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest();
        request.setStatus(PaymentStatus.COMPLETED);
        request.setTransactionId("txn_12345");

        Payment payment = createTestPayment();
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId("txn_12345");

        when(paymentService.updatePaymentStatus("pay_test123", PaymentStatus.COMPLETED, "txn_12345"))
                .thenReturn(payment);

        // When & Then - 添加认证头
        mockMvc.perform(put("/api/payments/pay_test123/status")
                        .header("Authorization", "Bearer mock-token")  // 添加认证头
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    void validatePayment_ReturnsTrue() throws Exception {
        // Given
        when(paymentService.validatePayment("pay_test123"))
                .thenReturn(true);

        // When & Then - 添加认证头
        mockMvc.perform(get("/api/payments/pay_test123/validate")
                        .header("Authorization", "Bearer mock-token"))  // 添加认证头
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void findPaymentByOrderAndStatus_Success() throws Exception {
        // Given
        Payment payment = createTestPayment();
        payment.setStatus(PaymentStatus.COMPLETED);

        when(paymentService.findByOrderIdAndStatus(1001L, PaymentStatus.COMPLETED))
                .thenReturn(payment);

        // When & Then - 添加认证头
        mockMvc.perform(get("/api/payments/order/1001/status/COMPLETED")
                        .header("Authorization", "Bearer mock-token"))  // 添加认证头
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentId").value("pay_test123"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }
}