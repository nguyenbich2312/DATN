package com.bzone.service;

import com.bzone.model.OrderStatus;
import com.bzone.repository.OrderStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DefaultOrderStatusService implements OrderStatusService {
    @Autowired
    private OrderStatusRepository orderStatusRepository;

    @Override
    public void addOrderStatus(OrderStatus orderStatus) {
        orderStatusRepository.save(orderStatus);
    }
}
