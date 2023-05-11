package com.bzone.controller.seller;

import com.bzone.model.Notification;
import com.bzone.model.Order;
import com.bzone.model.OrderStatus;
import com.bzone.repository.NotificationRepository;
import com.bzone.repository.OrderRepository;
import com.bzone.repository.OrderStatusRepository;
import com.bzone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@Transactional
public class SellerController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusRepository orderStatusRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/seller")
    public String showSellerPage(Model model) {

        List<Order> orders = orderRepository.findAll();
        List<Order> finalOrders = new ArrayList<>();
        for(Order order: orders){
            order.getOrderDate();
            OrderStatus orderStatusPending = orderStatusRepository.findByOrderAndOrderStatus(order, "Chờ xác nhận");
            if(orderStatusPending != null){
                OrderStatus orderStatus1 = orderStatusRepository.findByOrderAndOrderStatus(order, "Đã xác nhận");
                OrderStatus orderStatus2 = orderStatusRepository.findByOrderAndOrderStatus(order, "Đã hủy");
                if(orderStatus1 == null && orderStatus2 == null){
                    finalOrders.add(order);
                }
            }
        }
        finalOrders.sort((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()));
        model.addAttribute("orders", finalOrders);
        return "pages/seller/dashboard";
    }

    @GetMapping("/seller/view-order")
    public String showOrders(Model model, @RequestParam(value = "status", defaultValue = "") String status) {
        List<Order> orders = orderRepository.findAll();
        List<Order> finalOrders = new ArrayList<>();
        for(Order order: orders){
            if(order.getRecentOrderStatus().contains(status)){
                finalOrders.add(order);
            }
        }
        finalOrders.sort((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()));
        model.addAttribute("orders", finalOrders);
        model.addAttribute("status", status);
        return "pages/seller/view-order";
    }

    @GetMapping("/seller/accept-order")
    public String acceptOrder(@RequestParam long id) {
        Order order = orderRepository.findById(id);
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrder(order);
        orderStatus.setOrderStatus("Đã xác nhận");
        orderStatus.setStatusDate(new Date());
        orderStatusRepository.save(orderStatus);

        //add notification
        Notification notification = new Notification();
        notification.setUser(order.getUser());
        notification.setTitle("Đơn hàng #" + order.getOrderId() +  " đã được xác nhận");
        notification.setDescription("Đơn hàng #" + order.getOrderId() +  " đã được xác nhận");
        notification.setDate(new Date());
        notificationRepository.save(notification);
        return "redirect:/seller/view-order";
    }

    @GetMapping("/seller/reject-order")
    public String rejectOrder(@RequestParam long id) {
        Order order = orderRepository.findById(id);
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrder(order);
        orderStatus.setOrderStatus("Đã hủy");
        orderStatus.setStatusDate(new Date());
        orderStatusRepository.save(orderStatus);

        //add notification
        Notification notification = new Notification();
        notification.setUser(order.getUser());
        notification.setTitle("Đơn hàng #" + order.getOrderId() +  " đã bị hủy");
        notification.setDescription("Đơn hàng #" + order.getOrderId() +  " đã bị hủy");
        notification.setDate(new Date());
        notificationRepository.save(notification);
        return "redirect:/seller/view-order";
    }

    @GetMapping("/seller/view-detail")
    public String viewDetail(Model model, @RequestParam long id) {
        Order order = orderRepository.findById(id);
        model.addAttribute("order", order);
        return "pages/seller/view-detail";
    }
}
