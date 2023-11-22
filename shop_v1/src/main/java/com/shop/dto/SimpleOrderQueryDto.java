package com.shop.dto;

import com.shop.domain.Address;
import com.shop.domain.Order;
import com.shop.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SimpleOrderQueryDto {

    private Long orderId;

    private String name;

    private LocalDateTime orderDate;

    private OrderStatus orderStatus;

    private Address address;

    public SimpleOrderQueryDto(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStatus, Address address) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
    }
}
