package com.bzone.controller.client;

import com.bzone.model.*;
import com.bzone.repository.*;
import com.bzone.service.DefaultProductService;
import com.bzone.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class BaseController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private DefaultProductService productService;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private  ReviewRepository reviewRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrderRepository orderRepository;
    @GetMapping("/")
    public String index() {
        return "pages/client/home";
    }

    @GetMapping("/loadMenu")
    public void LoadMenu(@RequestParam(name = "id", defaultValue = "1") Long id, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset = UTF-8");
        Category Parent_cate = categoryRepository.findCategoryById(id);
        Set<Category> list_subCategory = categoryRepository.findByParentCategory_Id(id);
        response.getWriter().print("<div class=\"fhs_column_stretch\" style=\"padding-right: 12px;\">\n" +
                "                                <div class=\"fhs_menu_title fhs_center_left\" style=\"padding-left: 24px;\"><i\n" +
                "                                        class=\"ico_sachtrongnuoc\"></i><span class=\"menu-title\">" + Parent_cate.getCategoryName() + "</span><b\n" +
                "                                        class=\"caret\"></b></div>\n" +
                "                                <div class=\"fhs_menu_content fhs_column_left\">\n" +
                "                                    <div class=\"row\">");
        int sum = 0;
        for (Category i : list_subCategory) {
            sum++;
            if (sum > 8) {
                break;
            }
            response.getWriter().print("<div class=\"mega-col col-sm-3 \" data-widgets=\"wid-98\" data-colwidth=\"3\">\n" +
                    "                                            <div class=\"mega-col-inner\">\n" +
                    "                                                <div class=\"ves-widget\" data-id=\"wid-98\">\n" +
                    "                                                    <div class=\"widget-html\">\n" +
                    "                                                        <div class=\"widget-inner\">\n" +
                    "                                                            <h3><span style=\"color: #333333;\"><strong><a\n" +
                    "                                                                    style=\"font-weight: bold; font-size: 13px;\"\n" +
                    "                                                                    href=\"category?id="+i.getId() + "\"><span\n" +
                    "                                                                    style=\"color: #333333;\">" + i.getCategoryName() +
                    "                                                                                </span></a></strong></span></h3>\n" +
                    "                                                            <ul class=\"nav-links\">");
            int count = 0;
            for (Category a : i.getChildCategories()) {
                count++;
                if (count == 5) {
                    response.getWriter().print("<li style=\"color: #2489f4;\"><span\n" +
                            "                                                                        style=\"color: #2489f4;\"><a\n" +
                            "                                                                        href=\"search\"><span\n" +
                            "                                                                        style=\"color: #2489f4;\">Xem tất\n" +
                            "                                                                                cả</span></a></span></li>");
                    break;
                }
                response.getWriter().print("                                                                <li><a\n" +
                        "                                                                        href=\"category?id="+a.getId() +"\">" +
                        a.getCategoryName() +
                        "                                                                    </a></li>");
            }
            response.getWriter().print("                                                       </ul>\n" +
                    "                                                        </div>\n" +
                    "                                                    </div>\n" +
                    "                                                </div>\n" +
                    "                                            </div>\n" +
                    "                                        </div>");
        }
    }

    @GetMapping("/loadProduct")
    public void LoadProduct(@RequestParam(name = "id") String id, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset = UTF-8");
        Set<Product> productSet = productRepository.findByOrderByDiscountDesc();
        Set<Product> set;
        if (id.equals("deal")) {
            set = productRepository.findTopNewProduct();
        } else if (id.equals("km")) {
            set = productRepository.findTopDiscountProduct();
        } else if (id.equals("kmnv")) {
            set = productRepository.findBestsellerProduct();
        } else if (id.equals("mglnv")) {
            set = productService.getProductsByCategory(7, productSet).stream().limit(10).collect(Collectors.toSet());
        } else if (id.equals("topmanga")) {
            set = productService.getProductsByCategory(92, productSet).stream().limit(10).collect(Collectors.toSet());
        } else if (id.equals("lnvelmoi")) {
            set = productService.getProductsByCategory(71, productSet).stream().limit(10).collect(Collectors.toSet());
        } else {
            set = productService.getProductsByCategory(71, productSet).stream().limit(10).collect(Collectors.toSet());
        }
        response.getWriter().print("<ul class=\"bxslider\">\n" +
                "<li class=\"item items-sl-width\">");
        for (Product i : set) {
            if(!i.isAvailable()) continue;
            int rate = i.getRateSum()/Math.max(i.getRateCount(),1);
            int ratePercent = rate*100/5;
            Set<OrderDetail> orderDetails = i.getOrderDetails();
            int sum = 0;
            for (OrderDetail a : orderDetails) {
                sum += a.getQuantity();
            }
            response.getWriter().print("<div class=\"item-inner\" style=\"position: relative\">\n" +
                    "                                                <div class=\"label-pro-sale m-label-pro-sale\"><span\n" +
                    "                                                        class=\"p-sale-label discount-l-fs\">" + i.getDiscount() + "%</span></div>\n" +
                    "                                                <div class=\"ma-box-content\">\n" +
                    "                                                    <div class=\"products clearfix\">\n" +
                    "                                                        <div class=\"product images-container\"><a\n" +
                    "                                                                href=\"\\product?id="+ i.getId() + "\"\n" +
                    "                                                                title=\"" + i.getProductName() + "\"\n" +
                    "                                                                class=\"product-image\">\n" +
                    "                                                            <div class=\"product-image\"><img\n" +
                    "                                                                    src=\"" + i.getImage() + "\"\n" +
                    "                                                                    data-src=\"https://cdn0.fahasa.com/media/catalog/product/i/m/image_195509_1_46052.jpg\"\n" +
                    "                                                                    class=\" lazyloaded\"\n" +
                    "                                                                    alt=\"" + i.getProductName() + "\">\n" +
                    "                                                            </div>\n" +
                    "                                                        </a></div>\n" +
                    "                                                    </div>\n" +
                    "                                                    <h2 class=\"product-name-no-ellipsis\"><a\n" +
                    "                                                            href=\"\\product?id="+ i.getId() + "\"\n" +
                    "                                                            title=\" \">\n" + i.getProductName() +
                    "                                                    </h2>\n" +
                    "                                                    <div class=\"price-label\">\n" +
                    "                                                        <p class=\"special-price\"><span class=\"price-label\">Special\n" +
                    "                                                            Price</span><span id=\"product-price-298231\"\n" +
                    "                                                                              class=\"price m-price-font\">" + i.getFormattedPriceString(i.getPrice() * (100 - i.getDiscount()) / 100) + "đ</span>\n" +
                    "                                                        </p>\n" +
                    "                                                        <p class=\"old-price bg-white\"><span class=\"price-label\">Giá gốc:\n" +
                    "                                                        </span><span id=\"old-price-298231\"\n" +
                    "                                                                     class=\"price m-price-font\">" + i.getFormattedPriceString(i.getPrice()) + "đ</span></p>\n" +
                    "                                                    </div>\n" +
                    "                                                    <div class=\"fhs-rating-container\" style=\"height:20px\">\n" +
                    "                                                        <div class=\"ratings fhs-no-mobile-block\">\n" +
                    "                                                            <div class=\"rating-box\">\n" +
                    "                                                                <div class=\"rating\" style=\"width: " + ratePercent + "%" + "\"></div>\n" +
                    "                                                            </div>\n" +
                    "                                                            <div class=\"amount\">(" + i.getRateCount() + ")</div>\n" +
                    "                                                        </div>\n" +
                    "                                                    </div>\n" +
                    "                                                </div>\n" +
                    "                                                <div class=\"progress position-bar-gridslider color-progress-grid\">\n" +
                    "                                                    <div class=\"progress-bar color-bar-grid 298231-bar\"\n" +
                    "                                                         role=\"progressbar\"\n" +
                    "                                                         aria-valuenow=\"0\" aria-valuemin=\"0\" aria-valuemax=\"" + sum + "\"\n" +
                    "                                                         style=\"width: 0%;\"></div>\n" +
                    "                                                    <div class=\"text-progress-bar\"></div>\n" +
                    "                                                </div>\n" +
                    "                                            </div>");
        }
        response.getWriter().print("                                        </li>\n" +
                "                                    </ul>\n" +
                "                                        <a style=\"    display: flex;\n" +
                "    align-items: center;\n" +
                "    justify-content: center;\n" +
                "    max-width: 100%;\n" +
                "    max-height: 100%;\n" +
                "    border-radius: 8px;\n" +
                "    cursor: pointer;\n" +
                "    user-select: none;\n" +
                "    transition: all 0.5s;\n" +
                "    color: #C92127;\n" +
                "    background-color: #fff;\n" +
                "    border: 2px solid #C92127;\n" +
                "    font-size: 14px;\n" +
                "    font-weight: 700;\n" +
                "    width: 210px;\n" +
                "    height: 40px;    margin: 0 auto;\"   href=\"search?name=\">Xem Thêm</a>\n");
    }

    @GetMapping("/product")
    public String product(Model model, @RequestParam(name = "id") long id, Principal principal) {
        Product product = productRepository.findById(id);
        List<Product> products = productRepository.findAllByCategory( product.getCategory());
        products.remove(product);
        while (products.size() < 5 && products.size() > 0) {
            products.addAll(products);
        }
        List<Category> categories = new ArrayList<>();
        Category c = product.getCategory();
        while (c.getId() != c.getParentCategory().getId()) {
            categories.add(c);
            c = c.getParentCategory();
        }
        categories.add(c);
        Collections.reverse(categories);
        model.addAttribute("product", product);
        if(products.size() >= 5) {
            model.addAttribute("products", products.subList(0, 5));
        } else {
            model.addAttribute("products", products);
        }
        model.addAttribute("categories", categories);
        if(principal != null) {
            User user = userRepository.findByEmail(principal.getName());
            Review review = reviewRepository.findByUserAndProduct(user, product);
            model.addAttribute("review", review);
        }

        int rateCount = reviewRepository.countAllByProduct(product);
        model.addAttribute("rateCount", rateCount);

        List<Review> reviews = reviewRepository.findAllByProduct(product);
        int rate5 = reviews.stream().filter(review -> review.getRate() == 5).collect(Collectors.toList()).size();
        int rate4 = reviews.stream().filter(review -> review.getRate() == 4).collect(Collectors.toList()).size();
        int rate3 = reviews.stream().filter(review -> review.getRate() == 3).collect(Collectors.toList()).size();
        int rate2 = reviews.stream().filter(review -> review.getRate() == 2).collect(Collectors.toList()).size();
        int rate1 = reviews.stream().filter(review -> review.getRate() == 1).collect(Collectors.toList()).size();
        model.addAttribute("rate5", rate5*100/Math.max(rateCount, 1));
        model.addAttribute("rate4", rate4*100/Math.max(rateCount, 1));
        model.addAttribute("rate3", rate3*100/Math.max(rateCount, 1));
        model.addAttribute("rate2", rate2*100/Math.max(rateCount, 1));
        model.addAttribute("rate1", rate1*100/Math.max(rateCount, 1));
        return "pages/client/bookdetail";
    }


}
