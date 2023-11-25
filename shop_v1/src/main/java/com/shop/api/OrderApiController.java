package com.shop.api;

import com.shop.domain.*;
import com.shop.dto.OrderFlatDto;
import com.shop.dto.OrderItemQueryDto;
import com.shop.dto.OrderQueryDto;
import com.shop.repository.OrderRepository;
import com.shop.service.OrderQueryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryService orderQueryService;

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
     * OSIV(Open-Session-In-View)
     * OSIV 전략은 최초 데이터베이스 커넥션 시작 시점부터 API 응답 또는
     * 뷰 렌더링이 끝날 때 까지 영속성 컨텍스트와 데이터베이스 커넥션을 유지한다
     * 지연 로딩은 영속성 컨텍스트가 살아있어야 가능하고 영속성 컨텍스트는 기본적으로
     * 데이터베이스 커넥션을 유지한다. 이것 자체가 장점이자 단점이다
     *
     * OSIV 전략을 사용하지 않는다면 지연 로딩을 트랜잭션 안에서 처리해야 한다
     */
    @GetMapping("/api/v1-1/orders")
    public List<Order> orderV1_1() {
        return orderQueryService.getOrders();
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

    /**
     * 권장 순서
     * 1. 엔티티 조회 방식으로 우선 접근
     * 1-1. 페치 조인으로 쿼리 수 최적화
     * 1-2. 컬렉션 최적화 -> 페이징 필요 시 batch_size 로 최적화, 페이징 필요 없으면 페치 조인
     *
     * 2. 엔티티 조회 방식으로 해결안되면 DTO 조회 방식 사용
     * 3. DTO 조회 방식으로도 안된다면 Native Query or JdbcTemplate
     *
     * 엔티티 조회 방식은 페치 조인이나 배치 사이즈 등으로 코드를 거의 수정하지 않고 최적화 시도를 할 수 있다
     * 그러나 DTO 직접 조회의 경우 성능 최적화 방식을 변경할 때 많은 코드를 변경해야 한다
     * 엔티티 조회 방식은 JPA 가 많은 부분을 최적화 해주지만 DTO 조회 방식은 SQL 을 직접 다루는 것과 유사하기
     * 코드 복잡도와 성능 사이에서 줄타기를 해야 한다
     */

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
