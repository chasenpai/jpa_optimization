package com.shop.controller;

import com.shop.domain.item.Book;
import com.shop.domain.item.Item;
import com.shop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items/new")
    public String createForm(Model model) {
        model.addAttribute("form", new BookForm());
        return "items/createItemForm";
    }

    @PostMapping("/items/new")
    public String create(BookForm form) {

        Book book = new Book();
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());
        itemService.saveItem(book);

        return "redirect:/items";
    }

    @GetMapping(value = "/items/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {

        Book item = (Book) itemService.findOne(itemId);
        BookForm form = new BookForm();
        form.setId(item.getId());
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setStockQuantity(item.getStockQuantity());
        form.setAuthor(item.getAuthor());
        form.setIsbn(item.getIsbn());
        model.addAttribute("form", form);

        return "items/updateItemForm";
    }

//    @PostMapping("/items/{itemId}/edit")
    public String updateItemV1(@ModelAttribute("form") BookForm form) {

        //영속성 컨텍스트가 더는 관리하지 않는 엔티티를 준영속 엔티티라고 말한다
        //이미 DB에 저장되어 식별자가 존재하는 임의로 만들어낸 엔티티도 준영속 엔티티라 볼 수 있다
        //준영속 상태의 엔티티를 영속 상태로 변경할 땐 Merge 를 사용해야 한다
        Book book = new Book();
        book.setId(form.getId());
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());
        itemService.saveItem(book);

        return "redirect:/items";
    }

    @PostMapping("/items/{itemId}/edit")
    public String updateItemV2(@ModelAttribute("form") BookForm form) {
        //엔티티를 변경할 땐 항상 변경 감지를 이용
        //컨트롤러에서 엔티티를 생성하지 말고 트랜잭션이 있는 서비스 계층에 식별자와 변경할 데이터를 전달하자
        //DTO 또는 파라미터
        itemService.updateItem(form.getId(), form.getPrice(), form.getName(), form.getStockQuantity());
        return "redirect:/items";
    }

    @GetMapping("/items")
    public String list(Model model) {
        List<Item> items = itemService.findAll();
        model.addAttribute("items", items);
        return "items/itemList";
    }

}
