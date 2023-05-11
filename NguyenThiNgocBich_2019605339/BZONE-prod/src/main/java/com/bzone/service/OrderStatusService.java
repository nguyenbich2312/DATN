package com.bzone.service;

import com.bzone.model.OrderStatus;
import org.springframework.stereotype.Service;

@Service("")
public interface OrderStatusService {
    void addOrderStatus(OrderStatus orderStatus);
}
