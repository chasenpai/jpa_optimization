package com.shop.domain.item;

import com.shop.domain.Category;
import com.shop.exception.NotEnoughStockException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
public abstract class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    private String name;

    private int price;

    private int stockQuantity;

    @ManyToMany(mappedBy = "items") //실무에서는 사용하지 말자
    private List<Category> categories = new ArrayList<>();

    //setter 가 아닌 비즈니스 메서드로 변경하는 것이 좋다
    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
    }

    public void decreaseStock(int quantity) {
        int leftStock = this.stockQuantity - quantity;
        if(leftStock < 0) {
            throw new NotEnoughStockException("need more stock");
        }
        this.stockQuantity = leftStock;
    }
}
