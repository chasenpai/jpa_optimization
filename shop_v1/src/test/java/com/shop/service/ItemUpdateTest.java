package com.shop.service;

import com.shop.domain.item.Book;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ItemUpdateTest {

    @Autowired
    EntityManager em;

    @Test
    void update() {

        Book book = em.find(Book.class, 1L);

        //TX Begin
        book.setName("BookB");

        //Dirty Checking
        //TX Commit

    }

}
