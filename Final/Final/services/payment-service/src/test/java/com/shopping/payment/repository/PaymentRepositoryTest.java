package com.shopping.payment.repository;

import com.shopping.payment.config.TestSecurityConfig;
import com.shopping.payment.model.Payment;
import com.shopping.payment.model.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
class PaymentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockBean  // Mock KafkaTemplate
    private KafkaTemplate<?, ?> kafkaTemplate;

    @Test
    void findByPaymentId_Exists_ReturnsPayment() {
        // Given
        Payment payment = Payment.builder()
                .paymentId("pay_test123")
                .orderId(1001L)
                .userId(5001L)
                .amount(new BigDecimal("50.00"))
                .status(PaymentStatus.PENDING)
                .paymentMethod(com.shopping.payment.model.PaymentMethod.CREDIT_CARD)
                .currency("USD")
                .build();

        Payment savedPayment = entityManager.persistAndFlush(payment);

        // When
        Optional<Payment> found = paymentRepository.findByPaymentId("pay_test123");

        // Then
        assertTrue(found.isPresent());
        assertEquals("pay_test123", found.get().getPaymentId());
        assertEquals(1001L, found.get().getOrderId());
    }

    @Test
    void findByUserId_ReturnsUserPayments() {
        // Given
        Payment payment1 = Payment.builder()
                .paymentId("pay_test1")
                .orderId(1001L)
                .userId(5001L)
                .amount(new BigDecimal("50.00"))
                .status(PaymentStatus.PENDING)
                .paymentMethod(com.shopping.payment.model.PaymentMethod.CREDIT_CARD)
                .currency("USD")
                .build();

        Payment payment2 = Payment.builder()
                .paymentId("pay_test2")
                .orderId(1002L)
                .userId(5001L)
                .amount(new BigDecimal("75.00"))
                .status(PaymentStatus.COMPLETED)
                .paymentMethod(com.shopping.payment.model.PaymentMethod.CREDIT_CARD)
                .currency("USD")
                .build();

        entityManager.persist(payment1);
        entityManager.persist(payment2);
        entityManager.flush();

        // When
        List<Payment> userPayments = paymentRepository.findByUserId(5001L);

        // Then
        assertEquals(2, userPayments.size());
        assertTrue(userPayments.stream().allMatch(p -> p.getUserId().equals(5001L)));
    }

    @Test
    void existsByOrderIdAndStatusIn_Exists_ReturnsTrue() {
        // Given
        Payment payment = Payment.builder()
                .paymentId("pay_test123")
                .orderId(1001L)
                .userId(5001L)
                .amount(new BigDecimal("50.00"))
                .status(PaymentStatus.COMPLETED)
                .paymentMethod(com.shopping.payment.model.PaymentMethod.CREDIT_CARD)
                .currency("USD")
                .build();

        entityManager.persistAndFlush(payment);

        // When
        boolean exists = paymentRepository.existsByOrderIdAndStatusIn(
                1001L, List.of(PaymentStatus.COMPLETED, PaymentStatus.PROCESSING)
        );

        // Then
        assertTrue(exists);
    }
}