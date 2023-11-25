package com.shop.repository;

import com.shop.domain.Order;
import com.shop.domain.OrderSearch;
import com.shop.dto.OrderItemQueryDto;
import com.shop.dto.OrderQueryDto;
import com.shop.dto.SimpleOrderQueryDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long orderId) {
        return em.find(Order.class, orderId);
    }

    //JPQL 동적 쿼리를 문자로 생성하는 것은 번거롭고 실수로 인한 버그가 발생할 수 있다
    //실무에서는 QueryDsl 를 사용하자
    public List<Order> findOrders(OrderSearch orderSearch) {

        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }

        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }

        return query.getResultList();
    }

    public List<Order> findOrdersFetch() {
        return  em.createQuery(
                "select " +
                        "o from Order o " +
                        "join fetch o.member m " +
                        "join fetch o.delivery d ", Order.class)
                .getResultList();
    }

    //페이징 + 컬렉션 엔티티 함께 조회하는 방법
    //ToOne 관계를 모두 페치 조인하고 컬렉션은 지연 로딩으로 조회한다
    //지연 로딩 성능 최적화를 위해 @BatchSize 또는 글로벌 설정을 사용한다
    //배치 사이즈는 100 ~ 1000을 추천한다
    public List<Order> findOrdersFetchPaging(int offset, int limit) {
        return  em.createQuery(
                        "select " +
                                "o from Order o " +
                                "join fetch o.member m " +
                                "join fetch o.delivery d ", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<SimpleOrderQueryDto> findOrdersToDto() {
        //new 명령어를 사용해서 JPQL 의 결과를 DTO 로 즉시 반환
        //SELECT 절에서 원하는 데이터를 직접 선택하기 때문에 성능이 향샹(생각보다 미비)
        return  em.createQuery(
                "select " +
                            "new com.shop.dto.SimpleOrderQueryDto(" +
                                    " o.id, " +
                                    "m.name," +
                                    "o.orderDate, " +
                                    "o.status, " +
                                    "d.address" +
                            ") " +
                        "from " +
                            "Order o " +
                        "join " +
                            "o.member m " +
                        "join " +
                            "o.delivery d", SimpleOrderQueryDto.class)
                .getResultList();
    }

    //일대다 조인이 있을 경우 데이터베이스의 row 가 증가한다
    //그 결과 같은 order 엔티티의 조회 수도 증가하게 된다 > distinct 사용
    //SQL 에 distinct 를 추가하고, 더해서 중복된 엔티티가 조회되면 애플리케이션에서 걸러준다
    //하이버네이트 6부터는 distinct 가 자동 적용
    //하지만 컬렉션 페치 조인 시 페이징이 불가능하다
    //하이버네이트가 경고 로그를 남기고 메모리에서 페이징 해버린다(매우 위험)
    //추가로 컬렉션 페치 조인은 1개만 사용할 수 있다
    public List<Order> findOrdersAndItems() {
        return  em.createQuery(
                "select " +
                            "distinct o " +
                        "from " +
                            "Order o " +
                        "join fetch " +
                            "o.member m " +
                        "join fetch " +
                            "o.delivery d " +
                        "join fetch " +
                            "o.orderItems oi " +
                        "join fetch " +
                            "oi.item i", Order.class).getResultList();
    }

    //ToOne 관계는 조인해도 데이터 row 가 증가하지 않음
    //ToMany 관계는 조인하면 row 수가 증가
    //ToOne 관계들을 먼저 조회하고 ToMany 관계는 별도로 처리
    //루트 1번, 컬렉션 N번
    //단건 조회에서 많이 사용하는 방식
    public List<OrderQueryDto> findOrdersAndItemsToDto() {

        List<OrderQueryDto> orders = getOrders();
        orders.forEach(o -> {
            List<OrderItemQueryDto> orderItems = getOrderItems(o);
            o.setOrderItems(orderItems);
        });

        return orders;
    }

    private List<OrderQueryDto> getOrders() {
        return em.createQuery(
                "select " +
                            "new com.shop.dto.OrderQueryDto( " +
                            "o.id, " +
                            "m.name," +
                            "o.orderDate, " +
                            "o.status, " +
                            "d.address" +
                        ") " +
                        "from " +
                            "Order o " +
                        "join " +
                            "o.member m " +
                        "join " +
                            "o.delivery d ", OrderQueryDto.class).getResultList();
    }

    private List<OrderItemQueryDto> getOrderItems(OrderQueryDto o) {
        return em.createQuery(
                        "select " +
                                    "new com.shop.dto.OrderItemQueryDto( " +
                                    "oi.order.id, " +
                                    "i.name, " +
                                    "oi.orderPrice, " +
                                    "oi.count " +
                                ") " +
                                "from " +
                                    "OrderItem oi " +
                                "join " +
                                    "oi.item i " +
                                "where " +
                                    "oi.order.id =: orderId", OrderItemQueryDto.class)
                .setParameter("orderId", o.getOrderId())
                .getResultList();
    }

    //식별자로 ToMany 관계인 OrderItem 을 in 절로 조회
    //루트 1번, 컬렉션 1번
    //MAP 을 사용하여 성능 향상
    public List<OrderQueryDto> findOrdersAndItemsToDtoV2() {

        List<OrderQueryDto> orders = getOrders();

        List<Long> orderIds = orders.stream()
                .map(OrderQueryDto::getOrderId)
                .collect(Collectors.toList());

        List<OrderItemQueryDto> orderItems = getOrderItems(orderIds);

        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));

        orders.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return orders;
    }

    private List<OrderItemQueryDto> getOrderItems(List<Long> orderIds) {
        return em.createQuery(
                        "select " +
                                    "new com.shop.dto.OrderItemQueryDto( " +
                                    "oi.order.id, " +
                                    "i.name, " +
                                    "oi.orderPrice, " +
                                    "oi.count " +
                                ") " +
                                "from " +
                                    "OrderItem oi " +
                                "join " +
                                    "oi.item i " +
                                "where " +
                                    "oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();
    }

}
