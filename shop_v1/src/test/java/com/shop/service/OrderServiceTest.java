package com.shop.service;

import com.shop.domain.Address;
import com.shop.domain.Member;
import com.shop.domain.Order;
import com.shop.domain.OrderStatus;
import com.shop.domain.item.Book;
import com.shop.domain.item.Item;
import com.shop.exception.NotEnoughStockException;
import com.shop.repository.OrderRepository;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    EntityManager em;

    @Test
    void order() {

        Member member = createMember();
        Item book = createBook();

        int orderCount = 2;
        long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        Order findOrder = orderRepository.findOne(orderId);

        assertThat(findOrder.getStatus()).isEqualTo(OrderStatus.ORDER);
        assertThat(findOrder.getOrderItems().size()).isEqualTo(1);
        assertThat(findOrder.getTotalPrice()).isEqualTo(10000 * orderCount);
        assertThat(book.getStockQuantity()).isEqualTo(10 - orderCount);
    }

    @Test
    void quantityNotEnough() {

        Member member = createMember();
        Item book = createBook();

        int orderCount = 11;
        assertThatThrownBy(() -> orderService.order(member.getId(), book.getId(), orderCount))
                .isInstanceOf(NotEnoughStockException.class);
    }

    @Test
    void cancelOrder() {

        Member member = createMember();
        Item book = createBook();

        int orderCount = 2;
        long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        orderService.cancelOrder(orderId);

        Order canceledOrder = orderRepository.findOne(orderId);

        assertThat(canceledOrder.getStatus()).isEqualTo(OrderStatus.CANCEL);
        assertThat(book.getStockQuantity()).isEqualTo(10);
    }

    private Item createBook() {
        Item book = new Book();
        book.setName("bookA");
        book.setPrice(10000);
        book.setStockQuantity(10);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("memberA");
        member.setAddress(new Address("부산", "광안리", "51-211"));
        em.persist(member);
        return member;
    }

}