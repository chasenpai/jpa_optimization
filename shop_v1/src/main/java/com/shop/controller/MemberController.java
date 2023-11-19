package com.shop.controller;

import com.shop.domain.Address;
import com.shop.domain.Member;
import com.shop.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/members/new")
    public String createForm(Model model) {
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }

    /**
     * 폼 객체 vs 엔티티 직접 사용
     * 요구사항이 정말 단순할 땐 폼 객체 없이 엔티티를 직접 등록과 수정 화면에서 사용해도 된다
     * 하지만 요구사항이 복잡해지면 엔티티에 화면을 처리하기 위한 기능이 점점 증가하고
     * 엔티티는 화면에 종속적으로 변하게 된다
     * 실무에서 엔티티는 핵심 비즈니스 로직만 가지고 있고 화면을 위한 로직은 없어야 한다
     * DTO 또는 FORM 객체를 사용하고 엔티티는 최대한 순수하게 유지하자
     */
    @PostMapping("/members/new")
    public String create(@Valid MemberForm form, BindingResult result) {

        if (result.hasErrors()) {
            return "members/createMemberForm";
        }

        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());
        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);

        memberService.join(member);

        return "redirect:/";
    }

    @GetMapping(value = "/members")
    public String list(Model model) {
        List<Member> members = memberService.findMembers();
        model.addAttribute("members", members);
        return "members/memberList";
    }

}
