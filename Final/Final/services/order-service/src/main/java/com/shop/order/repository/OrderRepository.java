package com.shop.order.repository;

import com.shop.order.model.Order;
import com.shop.order.model.OrderStatus;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends CassandraRepository<Order, String> {


    @Query("SELECT * FROM orders WHERE userid = :userId ALLOW FILTERING")
    List<Order> findByUserId(@Param("userId") String userId);


    @Query("SELECT * FROM orders WHERE status = :status ALLOW FILTERING")
    List<Order> findByStatus(@Param("status") OrderStatus status);


    @Query("SELECT * FROM orders WHERE userid = :userId AND status = :status ALLOW FILTERING")
    List<Order> findByUserIdAndStatus(@Param("userId") String userId, @Param("status") OrderStatus status);


    boolean existsByOrderId(String orderId);


    @Query("SELECT * FROM orders WHERE createdat >= :startTime AND createdat <= :endTime ALLOW FILTERING")
    List<Order> findByCreatedAtBetween(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime);


    @Query("SELECT * FROM orders WHERE userid = :userId AND createdat >= :startTime AND createdat <= :endTime ALLOW FILTERING")
    List<Order> findByUserIdAndCreatedAtBetween(
            @Param("userId") String userId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);


    @Query("SELECT * FROM orders WHERE orderid = :orderId AND userid = :userId ALLOW FILTERING")
    Optional<Order> findByOrderIdAndUserId(@Param("orderId") String orderId, @Param("userId") String userId);


    @Query("SELECT * FROM orders WHERE totalamount >= :minAmount AND totalamount <= :maxAmount ALLOW FILTERING")
    List<Order> findByTotalAmountBetween(@Param("minAmount") Double minAmount, @Param("maxAmount") Double maxAmount);


    @Query("SELECT COUNT(*) FROM orders WHERE userid = :userId")
    long countByUserId(@Param("userId") String userId);


    @Query("SELECT COUNT(*) FROM orders WHERE status = :status")
    long countByStatus(@Param("status") OrderStatus status);


    @Query("DELETE FROM orders WHERE orderid = :orderId IF createdat < :expireTime")
    boolean deleteIfExpired(@Param("orderId") String orderId, @Param("expireTime") Instant expireTime);
}