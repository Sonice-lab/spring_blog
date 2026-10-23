package com.tenco.spring_blog.user;

import lombok.Getter;
import lombok.Setter;

/**
 * 세션 데이터 전용 DTO
 * - 로그인한 사용자를 식별하는데 필요한 값을 담는다.
 * - 비밀번호는 담지 않는다.
 * - 엔티티가 아니므로 값을 바꿔도 DB에 영향이 없다.
 */
@Setter
@Getter
public class SessionUserDto {
    private Long id;
    private String username;
    private String email;
    private boolean admin; // 관리자 여부 롬북에서는 isAdmin으로 호출할 수 있다.
    private String roleDisplay; // 화면 표시용 데이터(ADMIN 또는 USER)
    private String phoneNumber;

    public SessionUserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.admin = user.isAdmin(); // 관리자이면 true
        this.roleDisplay = this.admin ? "ADMIN" : "USER";
        this.phoneNumber = user.getPhoneNumber();
    }
}
