package com.tenco.spring_blog.user;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// [복합 유니크 제약]
// - 한 사용자는 같은 역할을 한 번만 가진다.
// 예) (1,ADMIN), (1,USER), (2, USER), 저장 가능 -> 만약 (1, USER를 한번 더 넣으면 DB가 거부함)
// columnNames에는 자바 필드명이 아닌 DB 컬럼명을 써야한다. 주의!!)
// 현재 user_id는 이 클래스에 필드로 없지만 User 엔티티 측에서
// @JoinColumn(name = user_id)를 설정하면 자동으로 컬럼이 만들어진다.
@Entity
@NoArgsConstructor
@Getter
@Table(name = "user_role_tb",
        uniqueConstraints = {@UniqueConstraint(name = "uk_user_role", columnNames = {"user_id", "role"})})

public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // enum을 숫자가 아닌 문자열(ADMIN, USER)로 저장
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Builder //id는 자동으로 채워지므로 빌더에서 제외
    public UserRole(Role role) {
        this.role = role;
    }


}
