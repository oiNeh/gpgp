package kr.co.gpgp.domain.order.entity;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import kr.co.gpgp.domain.delivery.entity.Delivery;
import kr.co.gpgp.domain.delivery.entity.enums.DeliveryStatusImpl;
import kr.co.gpgp.domain.order.enums.OrderStatus;
import kr.co.gpgp.domain.orderline.entity.OrderLine;
import kr.co.gpgp.domain.user.entity.User;
import kr.co.gpgp.web.exception.ErrorCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    @OneToMany(mappedBy = "order", cascade = ALL, orphanRemoval = true)
    private final List<OrderLine> orderLines = new ArrayList<>();

    @Enumerated(STRING)
    OrderStatus orderStatus;

    private Order(User user, Delivery delivery) {
        this.user = user;
        this.delivery = delivery;
        this.orderStatus = OrderStatus.ORDER;
    }

    public static Order of(User user, Delivery delivery, OrderLine... orderLines) {
        Order order = new Order(user, delivery);

        Arrays.stream(orderLines)
                .forEach(order::addOrderLine);
        return order;
    }

    // == 연관관계 메서드 == //
    public void addOrderLine(OrderLine orderLine) {
        orderLines.add(orderLine);
        orderLine.designateOrder(this);
    }

    public void cancel() {
        if (getDelivery().getStatus()
                .equals(DeliveryStatusImpl.DEPARTURE)) {
            throw new IllegalStateException(ErrorCode.UNABLE_TO_CANCEL_ORDER.getMessage());
        }

        if (getDelivery().getStatus()
                .equals(DeliveryStatusImpl.FINAL_DELIVERY)) {
            throw new IllegalStateException(ErrorCode.UNABLE_TO_CANCEL_ORDER.getMessage());
        }

        if (getDelivery().getStatus()
                .equals(DeliveryStatusImpl.NONE_TRACKING)) {
            throw new IllegalStateException(ErrorCode.UNABLE_TO_CANCEL_ORDER.getMessage());
        }

        changeStatus(OrderStatus.CANCEL);
        orderLines.forEach(OrderLine::cancel);
    }

    public void changeStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public int getTotalPrice() {
        return orderLines.stream()
                .mapToInt(OrderLine::getTotalPrice)
                .sum();
    }
}
