-- 创建测试表
CREATE TABLE IF NOT EXISTS payments (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        payment_id VARCHAR(50) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    transaction_id VARCHAR(100),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    description TEXT,
    idempotency_key VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    failure_reason TEXT
    );