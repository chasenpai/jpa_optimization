package com.shop.api;

import com.shop.domain.*;
import com.shop.repository.OrderRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    //컬렉션 조회 최적화

    /**
     *  V1 엔티티 직접 노출
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> orders = orderRepository.findOrders(new OrderSearch());
        for (Order order : orders) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return orders;
    }

    /**
     * V2 DTO 변환
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        return orderRepository.findOrders(new OrderSearch())
                .stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 페치 조인 최적화
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        return orderRepository.findOrdersAndItems()
                .stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 페이징 한계 돌파
     */
    @GetMapping("/api/v3-1/orders")
    public List<OrderDto> ordersV3_1(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {

        return orderRepository.findOrdersFetchPaging(offset, limit)
                .stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());
    }


    @Data
    static class OrderDto {

        private Long orderId;

        private String name;

        private LocalDateTime orderDate;

        private OrderStatus orderStatus;

        private Address address;

        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems()
                    .stream()
                    .map(OrderItemDto::new)
                    .collect(Collectors.toList());
        }

    }

    @Data
    static class OrderItemDto {

        private String itemName;

        private int orderPrice;

        private int count;

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }

}
