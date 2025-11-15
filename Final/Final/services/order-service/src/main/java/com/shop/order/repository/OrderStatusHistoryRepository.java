package com.shop.order.repository;

import com.shop.order.model.OrderStatusHistory;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderStatusHistoryRepository extends CassandraRepository<OrderStatusHistory, OrderStatusHistory.OrderStatusHistoryKey> {

    @Query("SELECT * FROM order_status_history WHERE order_id = :orderId ORDER BY updated_at DESC")
    List<OrderStatusHistory> findByKeyOrderIdOrderByKeyUpdatedAtDesc(@Param("orderId") String orderId);

    // 注意：OrderStatusHistory 实体中没有 status 字段，删除相关方法或添加 status 字段
    // 暂时注释掉使用 status 的方法，或者修改实体添加 status 字段

    /*
    @Query("SELECT * FROM order_status_history WHERE order_id = :orderId AND status = :status ORDER BY updated_at DESC")
    List<OrderStatusHistory> findByKeyOrderIdAndStatusOrderByKeyUpdatedAtDesc(
            @Param("orderId") String orderId,
            @Param("status") String status);
    */

    @Query("SELECT * FROM order_status_history WHERE order_id = :orderId AND updated_at >= :startTime AND updated_at <= :endTime ORDER BY updated_at DESC")
    List<OrderStatusHistory> findByKeyOrderIdAndUpdatedAtBetween(
            @Param("orderId") String orderId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);

    @Query("SELECT * FROM order_status_history WHERE order_id = :orderId ORDER BY updated_at DESC LIMIT 1")
    Optional<OrderStatusHistory> findLatestByKeyOrderId(@Param("orderId") String orderId);

    // 同样注释掉使用 status 的方法
    /*
    @Query("SELECT * FROM order_status_history WHERE status = :status ORDER BY updated_at DESC")
    List<OrderStatusHistory> findByStatusOrderByKeyUpdatedAtDesc(@Param("status") String status);
    */

    @Query("SELECT COUNT(*) FROM order_status_history WHERE order_id = :orderId")
    long countByKeyOrderId(@Param("orderId") String orderId);

    // 注释掉使用 status 的方法
    /*
    @Query("SELECT * FROM order_status_history WHERE status = :status AND updated_at >= :startTime AND updated_at <= :endTime ORDER BY updated_at DESC")
    List<OrderStatusHistory> findByStatusAndUpdatedAtBetween(
            @Param("status") String status,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);
    */

    @Query("SELECT * FROM order_status_history WHERE notes LIKE :notesPattern ALLOW FILTERING")
    List<OrderStatusHistory> findByNotesContaining(@Param("notesPattern") String notesPattern);

    @Query("DELETE FROM order_status_history WHERE order_id = :orderId")
    void deleteByKeyOrderId(@Param("orderId") String orderId);

    @Query("DELETE FROM order_status_history WHERE order_id = :orderId AND updated_at < :expireTime")
    void deleteByKeyOrderIdAndUpdatedAtBefore(
            @Param("orderId") String orderId,
            @Param("expireTime") Instant expireTime);

    @Query("SELECT * FROM order_status_history WHERE order_id = :orderId AND updated_at >= :fromTime ORDER BY updated_at DESC")
    List<OrderStatusHistory> findByKeyOrderIdAndUpdatedAtAfter(
            @Param("orderId") String orderId,
            @Param("fromTime") Instant fromTime);
}