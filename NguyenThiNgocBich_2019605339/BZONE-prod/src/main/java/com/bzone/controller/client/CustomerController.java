package com.bzone.controller.client;

import com.bzone.model.*;
import com.bzone.repository.*;
import com.bzone.service.DefaultEmailService;
import com.bzone.service.DefaultUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.text.DecimalFormat;
import java.util.*;

@Controller
@Transactional
public class CustomerController {
    private final static String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DefaultEmailService emailService;

    @Autowired
    private DefaultUserService userService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartDetailRepository cartDetailRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrderStatusRepository orderStatusRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private  NotificationRepository notificationRepository;

    @GetMapping("/signup")
    public String register(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("form", "signup");
        return "pages/client/login";
    }

    @PostMapping("/signup")
    public String register(@RequestParam("otp") String otp, Model model, HttpSession session) {
        User user = (User) session.getAttribute("register_user");
        if (otp.equals("")) {
            model.addAttribute("otp_message", "OTP is required.");
            model.addAttribute("form", "check");
            return "pages/client/login";
        }
        if (!otp.equals(user.getOtp())) {
            model.addAttribute("otp_message", "OTP is incorrect.");
            model.addAttribute("form", "check");
            return "pages/client/login";
        }
        if (new Date().getTime() - user.getOtpRequestedTime().getTime() > 300000) {
            model.addAttribute("otp_message", "OTP is expired.");
            model.addAttribute("form", "resent");
            return "pages/client/login";
        }
        try {
            session.removeAttribute("register_user");
            //set role for user
            user.setFirstName(user.getEmail().split("@")[0]);
            user.setLastName("");
            user.setActive(true);
            user.setRole(roleRepository.findByRoleName("Customer"));
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setDescription("Hãy bắt đầu cuộc hành trình thú vị của bạn tại nơi đây");
            notification.setTitle("Chào mừng bạn đến với BZone");
            notification.setDate(new Date());

            //save cart
            Cart cart = new Cart();
            cart.setTotalPrice(BigDecimal.valueOf(0));
            user.setCart(cart);

            userService.addUser(user);
            cart.setUser(user);
            cartRepository.save(cart);
            notificationRepository.save(notification);
        } catch (Exception e) {
        }
        return "redirect:/login";
    }

    @PostMapping("/signup/otp")
    public String sendOtp(@Valid User user, BindingResult result, Model model, @RequestParam("cf_password") String cfPassword, HttpSession session) {

        if (user.getPassword() != "" && !user.getPassword().matches(passwordRegex)) {
            model.addAttribute("form", "signup");
            result.rejectValue("password", "user.password", "Password must contain at least one digit, one lowercase, one uppercase, one special character and must be at least 8 characters long.");
        }

        if (result.hasFieldErrors("email") || result.hasFieldErrors("password")) {

            model.addAttribute("form", "signup");

            if (cfPassword.isEmpty())
                model.addAttribute("cf_password", "Confirm password is required");
            else if (!cfPassword.equals(user.getPassword()))
                model.addAttribute("cf_password", "Confirm password is not match");
            return "pages/client/login";
        }

        if (cfPassword.isEmpty()) {
            model.addAttribute("cf_password", "Confirm password is required");
            model.addAttribute("form", "signup");

            return "pages/client/login";
        } else if (!cfPassword.equals(user.getPassword())) {
            model.addAttribute("cf_password", "Confirm password is not match");
            model.addAttribute("form", "signup");

            return "pages/client/login";
        }

        if (userRepository.findByEmail(user.getEmail()) != null) {
            result.rejectValue("email", "user.email", "An account already exists for this email.");
            model.addAttribute("form", "signup");

            return "pages/client/login";
        }

        //send otp
        try {
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
                    "    <p>Thank you for choosing BZone. Use the following OTP to complete your Sign Up procedures. OTP is valid for 5 minutes</p>\n" +
                    "    <h2 style=\"background: #C92127;margin: 0 auto;width: max-content;padding: 0 10px;color: #fff;border-radius: 4px;\">" +
                    otp +
                    "</h2>\n" +
                    "    <p style=\"font-size:0.9em;\">Regards,<br />BZone</p>\n" +
                    "    <hr style=\"border:none;border-top:1px solid #eee\" />\n" +
                    "    <div style=\"float:right;padding:8px 0;color:#aaa;font-size:0.8em;line-height:1;font-weight:300\">\n" +
                    "      <p>BZone Inc</p>\n" +
                    "      <p>1600 Amphitheatre Parkway</p>\n" +
                    "      <p>California</p>\n" +
                    "    </div>\n" +
                    "  </div>\n" +
                    "</div>";

            emailService.sendEmail(user.getEmail(), "BZone - Email Verification", message);
            user.setOtp(otp);
            user.setOtpRequestedTime(new Date());
            session.setAttribute("register_user", user);
            model.addAttribute("form", "check");
            return "pages/client/login";
        } catch (Exception e) {
            return "error";
        }
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("form", "login");
        return "pages/client/login";
    }

    @PostMapping("/login-failed")
    public String login(@Valid User user, BindingResult result, Model model) {
        if (result.hasFieldErrors("email") || result.hasFieldErrors("password")) {
            model.addAttribute("form", "login");
            return "pages/client/login";
        }
        User existUser = userRepository.findByEmail(user.getEmail());
        if (existUser == null || !BCrypt.checkpw(user.getPassword(), existUser.getPassword())) {
            model.addAttribute("form", "login");
            model.addAttribute("message", "Email hoặc Mật khẩu sai!");
            return "pages/client/login";
        }
        if (!existUser.isActive()) {
            model.addAttribute("form", "login");
            model.addAttribute("message", "Tài khoản chưa được kích hoạt!");
            return "pages/client/login";
        }
        //authentication
        return "redirect:/";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("form", "forgot");
        return "pages/client/login";
    }

    @PostMapping("/forgot-password/otp")
    public String sendOtp(@Valid User user, BindingResult result, Model model, HttpSession session) {
        if (result.hasFieldErrors("email")) {
            model.addAttribute("form", "forgot");
            return "pages/client/login";
        }
        User existUser = userRepository.findByEmail(user.getEmail());
        if (existUser == null) {
            model.addAttribute("form", "forgot");
            result.rejectValue("email", "user.email", "Email không tồn tại!");
            return "pages/client/login";
        }
        if(result.hasFieldErrors("password")) {
            model.addAttribute("user", new User());
        }
        //send otp
        try {
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
                    "    <p>Thank you for choosing BZone. Use the following OTP to confirm your Reset Password procedures. OTP is valid for 5 minutes</p>\n" +
                    "    <h2 style=\"background: #C92127;margin: 0 auto;width: max-content;padding: 0 10px;color: #fff;border-radius: 4px;\">" +
                    otp +
                    "</h2>\n" +
                    "    <p style=\"font-size:0.9em;\">Regards,<br />BZone</p>\n" +
                    "    <hr style=\"border:none;border-top:1px solid #eee\" />\n" +
                    "    <div style=\"float:right;padding:8px 0;color:#aaa;font-size:0.8em;line-height:1;font-weight:300\">\n" +
                    "      <p>BZ Inc</p>\n" +
                    "      <p>1600 Amphitheatre Parkway</p>\n";
            emailService.sendEmail(user.getEmail(), "BZone - Reset Password", message);
            user.setOtp(otp);
            user.setOtpRequestedTime(new Date());
            session.setAttribute("forgot_user", user);
            model.addAttribute("form", "forgot_check");
            return "pages/client/login";
        } catch (Exception e) {
            return "error";
        }
    }

    @PostMapping("/forgot-password")
    public  String forgetPassword(@RequestParam("otp") String otp, @Valid User user, BindingResult result, Model model, HttpSession session, @RequestParam("cf_password") String cfPassword) {
        boolean check = false;
        if (user.getPassword() == "" || !user.getPassword().matches(passwordRegex)) {
            check = true;
            if(user.getPassword() != "")
                result.rejectValue("password", "user.password", "Password must contain at least one digit, one lowercase, one uppercase, one special character and must be at least 8 characters long.");
        }
        if (cfPassword.isEmpty()) {
            model.addAttribute("cf_password", "Confirm password is required");
            check = true;
        } else if (!cfPassword.equals(user.getPassword())) {
            model.addAttribute("cf_password", "Confirm password is not match");
            check = true;
        }
        User forgotUser = (User) session.getAttribute("forgot_user");
        if (otp.equals("")) {
            model.addAttribute("otp_message", "OTP is required.");
            check = true;
        }
        else if (!otp.equals(forgotUser.getOtp())) {
            model.addAttribute("otp_message", "OTP is incorrect.");
            check = true;
        }
        if (new Date().getTime() - forgotUser.getOtpRequestedTime().getTime() > 300000) {
            model.addAttribute("otp_message", "OTP is expired.");
            check = true;
        }
        if(check) {
            model.addAttribute("form", "forgot_check");
            return "pages/client/login";
        }
        User existUser = userRepository.findByEmail(forgotUser.getEmail());
        userService.changePassword(existUser, user.getPassword());
        model.addAttribute("user", new User());
        model.addAttribute("form", "login");
        return "pages/client/login";
    }

//    @GetMapping("/logout")
//    public String logout( HttpSession session, HttpServletResponse response) {
//        session.removeAttribute("user");
//        Cookie cookie = new Cookie("email", "");
//        cookie.setMaxAge(0);
//        response.addCookie(cookie);
//        return "redirect:/";
//    }
    @GetMapping("/user/cart")
    public String cart(Model model, Principal principal) {
        if (principal != null) {
            User user = userRepository.findByEmail(principal.getName());
            model.addAttribute("user", user);
            model.addAttribute("cart", user.getCart());
        }
        return "pages/client/cart";
    }

    @PostMapping("/user/cart/add")
    public String addToCart(@RequestParam(value = "type", defaultValue = "false") String type, @RequestParam("id") String id, @RequestParam("quantity") String quantity, Principal principal, HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        try{
            User user = userRepository.findByEmail(principal.getName());
            Product product = productRepository.findById(Integer.parseInt(id));
            Cart cart = user.getCart();
            CartDetail cartDetail = cartDetailRepository.findByCartAndProduct(cart, product);
            int oldQuantity = 0;
            if (cartDetail == null) {
                cartDetail = new CartDetail();
                cartDetail.setCart(cart);
                cartDetail.setProduct(product);
                cartDetail.setQuantity(Integer.parseInt(quantity));
            } else {
                oldQuantity = cartDetail.getQuantity();
                cartDetail.setQuantity(cartDetail.getQuantity() + Integer.parseInt(quantity));
            }
            if(cartDetail.getQuantity() > product.getQuantity()) {
                cartDetail.setQuantity(product.getQuantity());
            }
            if(cartDetail.getQuantity() < 1) {
//                product.getCartDetails().remove(cartDetail);
//                cart.getCartDetails().remove(cartDetail);
                cartDetail.setCart(null);
                cartDetail.setProduct(null);
                cartDetailRepository.delete(cartDetail);
                if(cartDetail.isSelected()) {
                    cart.setTotalPrice(BigDecimal.valueOf(cart.getTotalPrice().doubleValue() - oldQuantity * product.getDiscountPrice()));
                    cartRepository.save(cart);
                }
            } else {
                cartDetailRepository.save(cartDetail);
                if(cartDetail.isSelected()) {
                    cart.setTotalPrice(BigDecimal.valueOf(cart.getTotalPrice().doubleValue() + (cartDetail.getQuantity() - oldQuantity) * product.getDiscountPrice()));
                    cartRepository.save(cart);
                }
            }
            if(type.equals("true")) {
                return "redirect:/user/cart";
            }
            return "redirect:"+ referer;
        } catch (Exception e) {
            return "redirect:"+ referer;
        }
    }



    @PostMapping("/user/cart/select-all")
    public  String select(Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName());
        Cart cart = user.getCart();
        Set<CartDetail> cartDetails = cart.getCartDetails();
        double total = 0;
        for (CartDetail cartDetail : cartDetails.stream().toList()) {
            cartDetail.setSelected(true);
            cartDetailRepository.save(cartDetail);
            total += cartDetail.getProduct().getDiscountPrice() * cartDetail.getQuantity();
        }
        cart.setTotalPrice(BigDecimal.valueOf(total));
        cartRepository.save(cart);
        return "redirect:/user/cart";
    }

    @PostMapping("/user/cart/select")
    public  String select(@RequestParam("id") String id, Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName());
        Cart cart = user.getCart();
        CartDetail cartDetail = cartDetailRepository.findById(Integer.parseInt(id));
        double total = cart.getTotalPrice().doubleValue();
        if(!cartDetail.isSelected()) {
            total += cartDetail.getProduct().getDiscountPrice() * cartDetail.getQuantity();
        } else {
            model.addAttribute("selectAll", false);
            total -= cartDetail.getProduct().getDiscountPrice() * cartDetail.getQuantity();
            total = total < 0 ? 0 : total;
        }
        cartDetail.setSelected(!cartDetail.isSelected());
        cartDetailRepository.save(cartDetail);
        cart.setTotalPrice(BigDecimal.valueOf(total));
        cartRepository.save(cart);
        return "redirect:/user/cart";
    }

    @GetMapping("/user/order/checkout")
    public String checkout(Model model, Principal principal) {
        if (principal != null) {
            User user = userRepository.findByEmail(principal.getName());
            model.addAttribute("user", user);

            model.addAttribute("cart", user.getCart());

            String name = user.getFirstName() + " " + user.getLastName();
            String address = user.getAddress();
            String phone = user.getPhoneNumber();
            model.addAttribute("name", name);
            model.addAttribute("address", address);
            model.addAttribute("phone", phone);
        }
        model.addAttribute("name_msg", "");
        model.addAttribute("address_msg", "");
        model.addAttribute("phone_msg", "");
        model.addAttribute("df", new DecimalFormat("###,### đ"));
        return "pages/client/checkout_order";
    }

    @PostMapping("/user/order/confirm")
    public  String order(@RequestParam("name") String name, @RequestParam("address") String address, @RequestParam("phone") String phone, Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName());
        Cart cart = user.getCart();
        String name_msg = "";
        String address_msg = "";
        String phone_msg = "";
        if(name.isEmpty()) {
            name_msg = "Vui lòng nhập tên";
        }
        if(address.isEmpty()) {
            address_msg = "Vui lòng nhập địa chỉ";
        }
        if(phone.isEmpty()) {
            phone_msg = "Vui lòng nhập số điện thoại";
        } else if(!phone.matches("^[0-9]{10}$")) {
            phone_msg = "Số điện thoại không hợp lệ";
        }
        if(!name_msg.isEmpty() || !address_msg.isEmpty() || !phone_msg.isEmpty()) {
            model.addAttribute("name", name);
            model.addAttribute("address", address);
            model.addAttribute("phone", phone);
            model.addAttribute("user", user);
            model.addAttribute("name_msg", name_msg);
            model.addAttribute("address_msg", address_msg);
            model.addAttribute("phone_msg", phone_msg);
            model.addAttribute("cart", cart);
            model.addAttribute("df", new DecimalFormat("###,### đ"));
            return "pages/client/checkout_order";
        }
        Payment payment = paymentRepository.findById(1);
        Order order = new Order();
        order.setAddress(address);
        order.setUser(user);
        order.setPayment(payment);
        order.setPhoneNumber(phone);
        order.setName(name);
        order.setTotalPrice(BigDecimal.valueOf(cart.getTotalPrice().doubleValue() + 30000));
        order.setOrderDate(new Date());

        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setStatusDate(new Date());
        orderStatus.setOrderStatus("Chờ xác nhận");
        if(order.getOrderStatuses() != null) {
            order.getOrderStatuses().add(orderStatus);
        } else {
            order.setOrderStatuses(new HashSet<>(Arrays.asList(orderStatus)));
        }
        orderStatus.setOrder(order);
        orderRepository.save(order);
        orderStatusRepository.save(orderStatus);
        //userRepository.save(user);


        cart.setTotalPrice(BigDecimal.valueOf(0));
        cartRepository.save(cart);
        Set<CartDetail> cartDetails = cart.getCartDetails();
        for (CartDetail cartDetail : cartDetails.stream().toList()) {
            if(!cartDetail.isSelected()) continue;

            //update product quantity
            Product product = cartDetail.getProduct();
            product.setQuantity(Math.max(0, product.getQuantity() - cartDetail.getQuantity()));
            productRepository.save(product);

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setProduct(cartDetail.getProduct());
            orderDetail.setQuantity(cartDetail.getQuantity());
            orderDetail.setPrice(BigDecimal.valueOf(cartDetail.getProduct().getPrice()));
            orderDetail.setDiscount(cartDetail.getProduct().getDiscount());
            orderDetailRepository.save(orderDetail);
            cartDetail.setCart(null);
            cartDetail.setProduct(null);
            cartDetailRepository.delete(cartDetail);


        }


        // add notification
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Đơn hàng #" + order.getOrderId() + " đã được tạo");
        notification.setDescription("Đơn hàng #" + order.getOrderId() + " đã được tạo");
        notification.setDate(new Date());
        notificationRepository.save(notification);

        return "redirect:/user/account/history";
    }

    @PostMapping("/user/rate")
    public String rate(@RequestParam("rate_id") long id, @RequestParam("rate_star") int rate, Principal principal) {
        User user = userRepository.findByEmail(principal.getName());
        Product product = productRepository.findById(id);
        Review review = reviewRepository.findByUserAndProduct(user, product);
        if(review == null) {
            review = new Review();
            review.setProduct(product);
            review.setUser(user);
            review.setRate(rate);
            reviewRepository.save(review);
            product.setRateCount(product.getRateCount() + 1);
            product.setRateSum(product.getRateSum() + rate);
        } else {
            product.setRateSum(product.getRateSum() - review.getRate() + rate);
            review.setRate(rate);
            reviewRepository.save(review);
        }
        productRepository.save(product);
        return "redirect:/product?id=" + id;
    }
}