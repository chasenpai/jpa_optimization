package com.shop.service;

import com.shop.domain.*;
import com.shop.domain.item.Item;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import com.shop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public Long order(Long memberId, Long itemId, int count) {

        Member member = memberRepository.findOne(memberId);
        Item item = itemRepository.findOne(itemId);

        //배송정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());

        //주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        //주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);
        orderRepository.save(order);

        return order.getId();
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findOne(orderId);
        order.cancel();
    }

    /**
     * 주문과 취소 메서드를 보면 비즈니스 로직이 대부분 엔티티에 있다
     * 서비스 로직은 단순히 엔티티에 필요한 요청을 위임하는 역할을 한다
     * 이와 같이 엔티티가 비즈니스 로직을 가지고 객체 지향의 특성을 적극 활용하는 것을
     * 도메인 모델 패턴이라고 한다
     */
    public List<Order> findOrders(OrderSearch orderSearch) {
        return orderRepository.findOrders(orderSearch);
    }

}
