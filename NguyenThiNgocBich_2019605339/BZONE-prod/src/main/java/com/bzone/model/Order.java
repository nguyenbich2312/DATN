package com.bzone.model;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long orderId;

    @Column(name = "order_date")
    private Date orderDate;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "address")
    private String address;

    @Column(name = "phone")
    private String phoneNumber;

    @Column(name = "name")
    private String name;

    @ManyToMany(mappedBy = "orders")
    private Set<Product> products;

    @OneToMany(mappedBy = "order")
    private Set<OrderDetail> orderDetails;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_id", referencedColumnName = "payment_id")
    private Payment payment;

    @OneToMany(mappedBy = "order")
    private Set<OrderStatus> orderStatuses;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "shipper_id", referencedColumnName = "user_id")
    private User shipper;

    //equals method
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(orderId, order.orderId);
    }

    //hashCode method
    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }

    public String getRecentOrderStatus() {
        List<OrderStatus> orderStatusList = orderStatuses.stream().sorted((o1, o2) -> o2.getId().compareTo(o1.getId())).toList();
        return orderStatusList.get(0).getOrderStatus();
    }
}
