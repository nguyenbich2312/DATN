package com.bzone.controller.shipper;

import com.bzone.model.Notification;
import com.bzone.model.Order;
import com.bzone.model.OrderStatus;
import com.bzone.model.User;
import com.bzone.repository.NotificationRepository;
import com.bzone.repository.OrderRepository;
import com.bzone.repository.UserRepository;
import com.bzone.service.OrderStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
public class ShipperController {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderStatusService orderStatusService;

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/shipper")
    public String getDashBoard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User u = userRepository.findByEmail(email);
        List<Order> set = orderRepository.findAll();
        List<Order> list = new ArrayList<>();
        for (Order i : set) {
            if ((i.getOrderStatuses().size() == 2) ||
                    (i.getOrderStatuses().size() == 3 && i.getShipper().equals(u))) {
                for (OrderStatus j : i.getOrderStatuses()) {
                    if (j.getOrderStatus().trim().contains("Đã xác nhận")) {
                        list.add(i);
                    }
                }
            }
        }
        double sum = set.stream().filter(order -> order.getOrderStatuses().size() == 4 && order.getShipper().equals(u))
                .map(order -> order.getTotalPrice().doubleValue()).reduce(0.0, (aDouble, aDouble2) -> aDouble + aDouble2);
        double count = set.stream().filter(order -> order.getOrderStatuses().size() == 4 && order.getShipper().equals(u)).count();
        model.addAttribute("order_set", list);
        model.addAttribute("sum", sum);
        model.addAttribute("count", count);
        return "pages/shipper/dashboard";
    }

    @GetMapping("/shipper/history")
    public String getHistory(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User u = userRepository.findByEmail(email);
        Set<Order> set = orderRepository.findByShipper_Id(u.getId());
        List<Order> list = new ArrayList<>();
        for (Order i : set) {
            if (i.getOrderStatuses().size() == 4) {
                list.add(i);
            }
        }
        model.addAttribute("order_set", list);
        return "pages/shipper/history";
    }

    @GetMapping("/shipper/receiveOrder")
    public String receiveOrder(@RequestParam(name = "id") Optional<Long> id) {
        Long Orderid = id.get();
        Order order = orderRepository.findById(Orderid).get();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User u = userRepository.findByEmail(email);
        order.setShipper(u);
        orderRepository.save(order);
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderStatus("Đơn hàng đang giao");
        orderStatus.setOrder(order);
        orderStatus.setStatusDate(new Date());
        orderStatusService.addOrderStatus(orderStatus);

        //add notification
        Notification notification = new Notification();
        notification.setUser(u);
        notification.setTitle("Đơn hàng #" + order.getOrderId()+ " đang được giao");
        notification.setDescription("Đơn hàng #" + order.getOrderId()+ " đang được giao");
        notification.setDate(new Date());
        notificationRepository.save(notification);

        return "redirect:/shipper";
    }

    @GetMapping("/shipper/completeOrder")
    public String completeOrder(@RequestParam(name = "id") Optional<Long> id, @RequestParam(name = "message") Optional<String> message) {
        Long Orderid = id.get();
        Order order = orderRepository.findById(Orderid).get();
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderStatus(message.get());
        orderStatus.setOrder(order);
        orderStatus.setStatusDate(new Date());
        orderStatusService.addOrderStatus(orderStatus);

        //add notification
        Notification notification = new Notification();
        notification.setUser(order.getUser());
        notification.setTitle("Đơn hàng #" + order.getOrderId()+ " đã được giao");
        notification.setDescription("Đơn hàng #" + order.getOrderId()+ " đã được giao");
        notification.setDate(new Date());
        notificationRepository.save(notification);
        return "redirect:/shipper";
    }

    @GetMapping("/shipper/detail")
    public String getOrder(@RequestParam(name = "id") Optional<Long> id, Model model, HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        Long Orderid = id.get();
        Order order = orderRepository.findById(Orderid).get();
        model.addAttribute("order",order);
        model.addAttribute("url",referer);
        return "pages/shipper/DetailOrder";
    }
}
