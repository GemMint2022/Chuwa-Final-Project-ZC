package com.shop.order.repository;

import com.shop.order.model.OrderByUser;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface OrderByUserRepository extends CassandraRepository<OrderByUser, OrderByUser.OrderByUserKey> {


    @Query("SELECT * FROM orders_by_user WHERE user_id = :userId")
    List<OrderByUser> findByKeyUserId(@Param("userId") String userId);


    @Query("SELECT * FROM orders_by_user WHERE user_id = :userId ORDER BY created_at DESC")
    List<OrderByUser> findByKeyUserIdOrderByKeyCreatedAtDesc(@Param("userId") String userId);


    @Query("SELECT * FROM orders_by_user WHERE user_id = :userId AND status = :status ORDER BY created_at DESC")
    List<OrderByUser> findByKeyUserIdAndStatusOrderByKeyCreatedAtDesc(
            @Param("userId") String userId,
            @Param("status") String status);


    @Query("SELECT * FROM orders_by_user WHERE user_id = :userId AND created_at >= :startTime AND created_at <= :endTime ORDER BY created_at DESC")
    List<OrderByUser> findByKeyUserIdAndCreatedAtBetween(
            @Param("userId") String userId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);


    @Query("SELECT * FROM orders_by_user WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit")
    List<OrderByUser> findTopByKeyUserIdOrderByKeyCreatedAtDesc(
            @Param("userId") String userId,
            @Param("limit") int limit);


    @Query("SELECT * FROM orders_by_user WHERE user_id = :userId AND total_amount >= :minAmount ORDER BY created_at DESC")
    List<OrderByUser> findByKeyUserIdAndTotalAmountGreaterThanEqual(
            @Param("userId") String userId,
            @Param("minAmount") Double minAmount);


    @Query("DELETE FROM orders_by_user WHERE user_id = :userId")
    void deleteByKeyUserId(@Param("userId") String userId);


    @Query("DELETE FROM orders_by_user WHERE user_id = :userId AND order_id = :orderId")
    void deleteByKeyUserIdAndKeyOrderId(
            @Param("userId") String userId,
            @Param("orderId") String orderId);


    @Query("SELECT COUNT(*) FROM orders_by_user WHERE user_id = :userId")
    long countByKeyUserId(@Param("userId") String userId);

    @Query("SELECT COUNT(*) FROM orders_by_user WHERE user_id = :userId AND status = :status")
    boolean existsByKeyUserIdAndStatus(@Param("userId") String userId, @Param("status") String status);
}