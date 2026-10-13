package com.tenco.spring_blog.user;

import lombok.Getter;

/**
 * 세션 데이터 전용 DTO
 * - 로그인한 사용자를 식별하는데 필요한 값을 담는다.
 * - 비밀번호는 담지 않는다.
 * - 엔티티가 아니므로 값을 바꿔도 DB에 영향이 없다.
 */

@Getter
public class SessionUserDto {
    private Long id;
    private String username;
    private String email;

    public SessionUserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
    }
}
