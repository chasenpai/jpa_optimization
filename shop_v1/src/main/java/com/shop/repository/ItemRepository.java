package com.shop.repository;

import com.shop.domain.item.Item;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item) {
        if(item.getId() == null) {
            em.persist(item);
        }else{
            /**
             * 병합(Merge)
             * - 병합은 준영속 상태의 엔티티를 영속 상태로 변경할 때 사용하는 기능
             * - 준영속 엔티티의 식별자 값으로 영속 엔티티를 조회하고 영속 엔티티의 값을 준영속 엔티티의
             * - 값으로 모두 교체(병합)한 뒤, 트랜잭션 커밋 시점에 변경 감지 기능이 동작해서 UPDATE 쿼리가 나감
             * - 변경 감지 기능은 원하는 값만 변경할 수 있지만 병합은 모든 값을 변경한다
             * - 이는 병합 시 값이 없으면 null 로 업데이트가 될 위험도 있다
             * - 실무에서는 업데이트가 되는 값이 제한적인데, 병합은 모든 값을 변경해버리기 때문에 문제가 발생한다
             * - 문제를 해결하려면 모든 값을 변경 폼 화면에서 유지하고 있어야 한다
             * - 보통 실무에서는 변경가능한 데이터만 노출하기 때문에 병합을 사용하는것은 번거롭다
             */
            em.merge(item);
        }
    }

    public Item findOne(Long itemId) {
        return em.find(Item.class, itemId);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class).getResultList();
    }

}
