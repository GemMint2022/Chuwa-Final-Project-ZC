package com.shop.order.model;

import lombok.Data;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Data
@Table("orders_by_user")
public class OrderByUser {
    @PrimaryKey
    private OrderByUserKey key;

    private String status;
    private BigDecimal totalAmount;
    private Instant createdAt;

    @Data
    @PrimaryKeyClass
    public static class OrderByUserKey {
        @PrimaryKeyColumn(name = "user_id", type = PrimaryKeyType.PARTITIONED)
        private String userId;

        @PrimaryKeyColumn(name = "order_id", type = PrimaryKeyType.CLUSTERED)
        private String orderId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OrderByUserKey that = (OrderByUserKey) o;
            return Objects.equals(userId, that.userId) &&
                    Objects.equals(orderId, that.orderId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, orderId);
        }
    }
}