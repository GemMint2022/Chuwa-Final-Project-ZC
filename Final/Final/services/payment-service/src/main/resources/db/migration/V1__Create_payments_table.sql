CREATE TABLE IF NOT EXISTS payments (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        payment_id VARCHAR(50) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    transaction_id VARCHAR(100) UNIQUE,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    description TEXT,
    idempotency_key VARCHAR(100) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    failure_reason TEXT,

    INDEX idx_payment_id (payment_id),
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_idempotency_key (idempotency_key)
    );