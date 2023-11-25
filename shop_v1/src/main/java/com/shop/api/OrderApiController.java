package com.shop.api;

import com.shop.domain.*;
import com.shop.dto.OrderFlatDto;
import com.shop.dto.OrderItemQueryDto;
import com.shop.dto.OrderQueryDto;
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

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;

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

    /**
     * DTO 직접 조회
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> orderV4() {
        return orderRepository.findOrdersAndItemsToDto();
    }

    /**
     * DTO 직접 조회 최적화
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> orderV5() {
        return orderRepository.findOrdersAndItemsToDtoV2();
    }

    /**
     * DTO 직접 조회 한방 쿼리
     * - API 스펙과 맞추려면 애플리케이션 내에서 추가적인 작업 필요
     */
    @GetMapping("/api/v6/orders")
    public List<OrderFlatDto> orderV6() {
        return orderRepository.findOrdersAndItemsToDtoV3();
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
