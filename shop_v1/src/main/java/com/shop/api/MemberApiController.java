package com.shop.api;

import com.shop.domain.Member;
import com.shop.service.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    /**
     * V1 엔티티를 직접 매핑
     * - 엔티티에 프레젠테이션 계층을 위한 로직이 추가되고 엔티티에 API 검증을 위한 로직이 들어간다
     * - 실무에서는 회원 엔티티를 위한 다양한 API 가 만들어지는데 한 엔티티에 각각의 API 를 위한
     * - 모든 요청 요구사항을 담기는 어렵고 엔티티가 변경되면 API 스펙이 변한다
     */
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long memberId = memberService.join(member);
        return new CreateMemberResponse(memberId);
    }

    /**
     * V2 엔티티 대신 DTO 를 매핑
     * - 엔티티와 프레젠테이션 계층을 위한 로직을 분리할 수 있다
     * - 엔티티와 API 스펙을 명확하게 분리할 수 있으므로 엔티티가 변해도 API 스펙이 변하지 않는다
     * - 실무에선 절대 엔티티를 API 스펙에 노출하면 안된다
     */
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());
        Long memberId = memberService.join(member);
        return new CreateMemberResponse(memberId);
    }

    @PatchMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long memberId,
                                               @RequestBody @Valid UpdateMemberRequest request) {
        Member member = memberService.update(memberId, request.getName());
        return new UpdateMemberResponse(member.getId(), member.getName());
    }

    //컬렉션을 직접 반환하면 API 스펙을 변경하기 어렵고 엔티티가 변하면 API 스펙이 변한다
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    //별도의 Result 클래스로 컬렉션을 감싸면 필요한 필드를 추가할 수 있다
    @GetMapping("/api/v2/members")
    public Result membersV2() {
        List<MemberDto> data =  memberService.findMembers()
                .stream().map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());
        return new Result(data);
    }

    @Data
    static class CreateMemberRequest {
        @NotEmpty
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class CreateMemberResponse {
        private Long id;
    }

    @Data
    static class UpdateMemberRequest {
        @NotEmpty
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class Result<T>{
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }

}
