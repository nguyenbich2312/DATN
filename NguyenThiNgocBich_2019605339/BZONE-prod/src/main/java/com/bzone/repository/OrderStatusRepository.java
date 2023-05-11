package com.bzone.repository;

import com.bzone.model.Order;
import com.bzone.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderStatusRepository extends JpaRepository<OrderStatus, Long> {

    OrderStatus findById(long orderStatusId);
    List<OrderStatus> findByOrder_OrderIdOrderByIdDesc(Long orderId);

    OrderStatus findByOrderAndOrderStatus(Order order, String orderStatus);

    List<OrderStatus> findAllByOrderAndOrderStatus(Order order, String status);

    List<OrderStatus> findAllByOrder(Order order);
}

