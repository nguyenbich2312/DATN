package com.bzone.ulti;

import com.bzone.model.Order;
import com.bzone.model.OrderStatus;

import java.util.HashSet;
import java.util.Set;

public class OrderSupport {
    public static Set<Order> getOrderByStatus(Set<Order> all, String status) {
        Set<Order> list_order = new HashSet<>();
        if (status == null || status.equals("All")) {
            list_order = all;
        } else if (status.equals("pending")) {
            for (Order i : all) {
                if (i.getOrderStatuses().size() == 1) {
                    for (OrderStatus o : i.getOrderStatuses()) {
                        if (o.getOrderStatus().trim().contains("Chờ xác nhận")) {
                            list_order.add(i);
                        }
                    }
                }
            }
        } else if (status.equals("processing")) {
            for (Order i : all) {
                if (i.getOrderStatuses().size() == 3) {
                    for (OrderStatus o : i.getOrderStatuses()) {
                        if (o.getOrderStatus().equalsIgnoreCase("Đang giao hàng")) {
                            list_order.add(i);
                        }
                    }
                }
            }
        } else if (status.equals("complete")) {
            for (Order i : all) {
                if (i.getOrderStatuses().size() == 4) {
                    for (OrderStatus o : i.getOrderStatuses()) {
                        if (o.getOrderStatus().equalsIgnoreCase("Giao hàng thành công")) {
                            list_order.add(i);
                        }
                    }
                }
            }
        } else {
            for (Order i : all) {
                if (i.getOrderStatuses().size() == 2) {
                    for (OrderStatus o : i.getOrderStatuses()) {
                        if (o.getOrderStatus().equalsIgnoreCase("Đã hủy")) {
                            list_order.add(i);
                        }
                    }
                }
            }
        }
        return list_order;
    }
}
