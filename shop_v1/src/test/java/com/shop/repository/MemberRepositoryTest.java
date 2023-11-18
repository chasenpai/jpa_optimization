package com.shop.repository;

import com.shop.domain.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    @Transactional
    @Rollback(value = false)
    void testMember() throws Exception {

//        //given
//        Member member = new Member();
//        member.setUsername("memberA");
//
//        //when
//        Long savedId = memberRepository.save(member);
//
//        //then
//        Member findMember = memberRepository.find(savedId);
//        assertThat(findMember.getId()).isEqualTo(member.getId());
//        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
//        //같은 영속성 컨텍스트 안에서 ID 값이 같으면 같은 엔티티로 식별
//        assertThat(findMember).isEqualTo(member);
    }

}