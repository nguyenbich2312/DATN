package com.bzone.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

@Entity
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product")
public class Product {
    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Column(name = "product_name")
    private String productName;

    @NotEmpty
    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "price")
    private double price;

    @NotEmpty
    @Column(name = "image")
    private String image;

    @NotNull
    @Column(name = "quantity")
    private int quantity;

    @NotNull
    @Column(name = "discount")
    private int discount;

    @NotEmpty
    @Column(name = "isbn")
    private String isbn;

    @NotEmpty
    @Column(name = "author")
    private String author;


    @Column(name = "publisher")
    private String publisher;


    @Column(name = "publish_year")
    private String publishYear;

    @NotEmpty
    private String language;
    @NotEmpty
    private String supplier;

    @NotEmpty
    @Column(name = "book_layout")
    private String bookLayout;

    @Column(name = "available")
    private boolean available;

    @Column(name = "rate_sum")
    private int rateSum;

    @Column(name = "rate_count")
    private int rateCount;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "category_id", referencedColumnName = "category_id")
    private Category category;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "cartdetail",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "cart_id"))
    private Set<Cart> carts;

    @OneToMany(mappedBy = "product")
    private Set<CartDetail> cartDetails;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "orderdetail",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "order_id"))
    private Set<Order> orders;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "review",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> users;

    @OneToMany(mappedBy = "product")
    private Set<Review> reviews;

    @OneToMany(mappedBy = "product")
    private Set<OrderDetail> orderDetails;

    public String getFormattedPriceString(double price) {
        long n = (long) price - (long) (price % 1000);
        String s = n+ "";
        s=s.trim();
        String priceString = "";
        for(int i=s.length()-1;i>=0;i--) {
            priceString = s.charAt(i) + priceString;
            if((s.length()-i) % 3 == 0 && i!=0)
                priceString = "." + priceString;
        }
        return priceString;
    }

    public String getFormattedPrice() {
        return getFormattedPriceString(price);
    }

    public String getFormattedDiscountPrice() {
        return getFormattedPriceString(price - (price * discount / 100));
    }

    public double getDiscountPrice() {
        double discountPrice = price - (price * discount / 100);
        return discountPrice - (discountPrice % 1000);
    }

    //equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    //hashCode
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public double getInt() {
        return Integer.parseInt(publishYear);
    }

    public double getCost() {
        return Integer.parseInt(author);
    }
}
