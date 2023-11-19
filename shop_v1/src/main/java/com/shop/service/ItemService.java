package com.shop.service;

import com.shop.domain.item.Book;
import com.shop.domain.item.Item;
import com.shop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    /**
     * 변경 감지
     * - 영속성 컨텍스트에서 엔티티를 다시 조회한 후 데이터를 수정하는 방법
     * - 트랜잭션 안에서 엔티티를 다시 조회하고 값을 변경해주면
     * - 트랜잭션 커밋 시점에 변경을 감지하고 UPDATE 쿼리를 날림
     */
    @Transactional
    public void updateItem(Long itemId, int price, String name, int stock) {
        Item findItem = itemRepository.findOne(itemId);
        findItem.setPrice(price);
        findItem.setName(name);
        findItem.setStockQuantity(stock);
    }

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }

}
