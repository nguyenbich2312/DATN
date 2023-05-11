package com.bzone.controller.client;

import com.bzone.model.*;
import com.bzone.repository.*;
import com.bzone.service.EmailService;
import com.bzone.service.UserService;
import com.bzone.ulti.OrderSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class CustomerInfomationController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserService userService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OrderStatusRepository orderStatusRepository;

    @GetMapping("/user/account/profile")
    public String showProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email);
        if (user != null) {
            Calendar cal = Calendar.getInstance();
            if (user.getDob() != null) {
                cal.setTime(user.getDob());
                int day = cal.get(Calendar.DAY_OF_MONTH);
                int month = cal.get(Calendar.MONTH);
                int year = cal.get(Calendar.YEAR);
                model.addAttribute("day", day);
                model.addAttribute("month", month + 1);
                model.addAttribute("year", year);
            }
        }
        model.addAttribute("user", user);
        return "pages/client/Profile";
    }

    @PostMapping("/user/account/profile")
    public String updateProfile(@RequestParam(name = "id") Long id, @RequestParam(name = "lastname") String lastname, @RequestParam(name = "firstname") String firstname,
                                @RequestParam(name = "telephone") String phone, @RequestParam(name = "email") String email, @RequestParam(name = "gender-radio") String gender,
                                @RequestParam(name = "day") String day, @RequestParam(name = "month") String month, @RequestParam(name = "year") String year,
                                @RequestParam(name = "current_password") String oldPass, @RequestParam(name = "password") String newPass, @RequestParam(name = "confirmation") String confirm,
            @RequestParam(name = "change_password") Optional<String> check
            , HttpServletRequest request, HttpServletResponse response, Model model) {
        User user = userRepository.findById(id).get();
        String date_String = year + "-" + day + "-" + month;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-dd-MM");
        try{
            formatter.setLenient(false);
            Date date = formatter.parse(date_String);
            user.setDob(date);
        }
        catch (ParseException e) {
            model.addAttribute("error", "date wrong!");
            return showProfile(model);
        }
        if(check.isPresent()){
            if (oldPass != null && BCrypt.checkpw(oldPass, user.getPassword())) {
                if (newPass.equals(confirm)) {
                    user.setPassword(BCrypt.hashpw(newPass, BCrypt.gensalt()));
                } else {
                    model.addAttribute("error", "Password and Re-Password not match");
                    return showProfile(model);
                }
            } else {
                model.addAttribute("error", "Old Password is wrong!");
                return showProfile(model);
            }
        }
        user.setRole(roleRepository.findByRoleName("Customer"));
        try {
            userService.addUser(user);
        } catch (Exception ex) {

        }
        user.setFirstName(firstname);
        user.setLastName(lastname);
        user.setPhoneNumber(phone);
        user.setEmail(email);
        user.setGender(gender.equalsIgnoreCase("male"));
        model.addAttribute("report", "Save successful!!");
        return showProfile(model);
    }

    @GetMapping("/user/account/history")
    public String getHistoryOrder(@RequestParam(name = "status") Optional<String> sta, HttpServletRequest request, HttpServletResponse response, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User u = userRepository.findByEmail(email);
        String status = sta.orElse(null);
        if (u == null) {
            return "redirect:/login";
        }
        Set<Order> all = orderRepository.findByUser_Id(u.getId());
        Set<Order> list_order = OrderSupport.getOrderByStatus(all, status);
        int total = OrderSupport.getOrderByStatus(all, "All").size();
        int pending = OrderSupport.getOrderByStatus(all, "pending").size();
        int process = OrderSupport.getOrderByStatus(all, "processing").size();
        int complete = OrderSupport.getOrderByStatus(all, "complete").size();
        int cancel = OrderSupport.getOrderByStatus(all, "cancel").size();
        model.addAttribute("list", list_order);
        model.addAttribute("total", total);
        model.addAttribute("pending", pending);
        model.addAttribute("process", process);
        model.addAttribute("complete", complete);
        model.addAttribute("cancel", cancel);
        return "pages/client/Order";
    }

    @GetMapping("/user/account/notify")
    public String getNotification(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User u = userRepository.findByEmail(email);
        if (u == null) {
            return "redirect:/login";
        }
        Set<Notification> list_notify = notificationRepository.findByUser_Id(u.getId());
        model.addAttribute("list_noti", list_notify);
        return "pages/client/Notification";
    }

    @GetMapping("/user/account/otp")
    public void sendOTP(HttpServletResponse response, @RequestParam(name = "email") String email) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            if (userRepository.findByEmail(email) != null) {
                response.getWriter().print("Email existed!");
                return;
            }
            Random rand = new Random();
            String otp = rand.nextInt(999999) + "";
            while (otp.length() < 6) {
                otp = "0" + otp;
            }
            String message = "<div style=\"font-family: Helvetica,Arial,sans-serif;min-width:1000px;overflow:auto;line-height:2\">\n" +
                    "  <div style=\"margin:50px auto;width:70%;padding:20px 0\">\n" +
                    "    <div style=\"border-bottom:1px solid #eee\">\n" +
                    "      <a href=\"\" style=\"font-size:1.4em;color: #C92127;text-decoration:none;font-weight:600\">BZone</a>\n" +
                    "    </div>\n" +
                    "    <p style=\"font-size:1.1em\">Hi,</p>\n" +
                    "    <p>Thank you for choosing BZone. Use the following OTP to confirm your Change EMail procedures. OTP is valid for 5 minutes</p>\n" +
                    "    <h2 style=\"background: #C92127;margin: 0 auto;width: max-content;padding: 0 10px;color: #fff;border-radius: 4px;\">" +
                    otp +
                    "</h2>\n" +
                    "    <p style=\"font-size:0.9em;\">Regards,<br />BZone</p>\n" +
                    "    <hr style=\"border:none;border-top:1px solid #eee\" />\n" +
                    "    <div style=\"float:right;padding:8px 0;color:#aaa;font-size:0.8em;line-height:1;font-weight:300\">\n" +
                    "      <p>BZone Inc</p>\n" +
                    "      <p>1600 Amphitheatre Parkway</p>\n";
            emailService.sendEmail(email, "BZone - Change OTP", message);
            response.getWriter().print(otp);
            User user = userRepository.findByEmail(auth.getName());
            user.setOtp(otp);
            user.setOtpRequestedTime(new Date());
            userService.addUser(user);
        } catch (Exception e) {

        }
    }

    @GetMapping("/user/account/detailOrder")
    public String getOrderDetail(@RequestParam(name = "id") Optional<Long> Order_id, Model model) {
        Long id = Order_id.get();
        Order order = orderRepository.findById(id).get();
        List<OrderStatus> orderStatus = orderStatusRepository.findByOrder_OrderIdOrderByIdDesc(id);
        Collections.sort(orderStatus,(o1, o2) -> {
            return (int)(o1.getId() - o2.getId());
        });
        double sum = 0;
        for(OrderDetail i : order.getOrderDetails()){
            sum += i.getQuantity() * Math.floor(i.getPrice().doubleValue() * (100 - i.getDiscount()) / 100000) * 1000;
        }
        model.addAttribute("sum",Math.floor(sum/1000));
        model.addAttribute("last_status", orderStatus);
        model.addAttribute("order", order);
        return "pages/client/OrderDetail";
    }

    @GetMapping("/user/cancelOrder")
    public String cancelOrder(@RequestParam(name = "id") Optional<Long> Order_id){
        Long id = Order_id.get();
        Order order = orderRepository.findById(id).get();
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrder(order);
        orderStatus.setOrderStatus("Đã hủy");
        orderStatus.setStatusDate(new Date());
        orderStatusRepository.save(orderStatus);
        return "redirect:/user/account/detailOrder?id="+id.longValue();
    }
}
