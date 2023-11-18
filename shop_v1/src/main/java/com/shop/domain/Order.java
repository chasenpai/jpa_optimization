package com.shop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
//엔티티에서는 가급적 setter 를 사용하지 말자 - setter 가 모두 열려있다면 변경 포인트가 많아져 유지보수가 어렵다
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    //모든 연관관계는 지연로딩으로 설정하자 - 즉시로딩은 예측이 어렵고 N + 1 문제가 발생할 수 있다
    //연관된 엔티티를 함께 조회해야 한다면 fetch join 또는 엔티티 그래프 기능을 사용하자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    //컬렉션은 필드에서 초기화 하자 - null 문제에서 안전해지고 하이버네이트는 엔티티를 영속화 할 때
    //컬렉션을 감싸서 하이버네이트가 제공하는 내장 컬렉션으로 변경하기 때문에 하이버네이트
    //내부 메커지즘에 문제가 발생할 수 있다
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    //연관관게 편의 메서드 - 연관관계의 주인이 들고있는게 좋다
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }
}
