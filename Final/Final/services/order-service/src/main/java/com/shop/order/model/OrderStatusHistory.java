package com.shop.order.model;

import lombok.Data;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.Objects;

@Data
@Table("order_status_history")
public class OrderStatusHistory {
    @PrimaryKey
    private OrderStatusHistoryKey key;

    private String notes;

    public OrderStatusHistory() {
    }

    @Data
    @PrimaryKeyClass
    public static class OrderStatusHistoryKey {
        @PrimaryKeyColumn(name = "order_id", type = PrimaryKeyType.PARTITIONED)
        private String orderId;

        @PrimaryKeyColumn(name = "updated_at", type = PrimaryKeyType.CLUSTERED)
        private Instant updatedAt;

        public OrderStatusHistoryKey() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OrderStatusHistoryKey that = (OrderStatusHistoryKey) o;
            return Objects.equals(orderId, that.orderId) &&
                    Objects.equals(updatedAt, that.updatedAt);
        }

        @Override
        public int hashCode() {
            return Objects.hash(orderId, updatedAt);
        }
    }
}