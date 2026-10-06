package com.tenco.spring_blog.user;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

// [ 1. 클래스 레벨 애노테이션 ]
@Getter // 모든 필드의 Getter 메서드(getId(), getUsername() 등)를 자동 생성합니다.
@NoArgsConstructor // 매개변수가 없는 기본 생성자를 만듭니다. (JPA 엔티티 생성 시 필수)
@Table(name = "user_tb") // DB에 생성될 테이블 이름을 "user_tb"로 명시적으로 지정합니다.
@Entity // 이 클래스가 DB 테이블과 1:1로 매핑되는 JPA '엔티티' 객체임을 선언합니다.
public class User {
    // [ 2. 기본 키(PK) 관련 애노테이션 ]
    @Id // 해당 필드를 테이블의 식별자(Primary Key)로 지정합니다.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 생성을 DB에 맡깁니다. (MySQL 등의 Auto Increment 기능 활용)
    private Long id;

    // [ 3. 일반 컬럼 관련 애노테이션 ]
    @Column(unique = true) // 컬럼에 유니크(고유) 제약 조건을 걸어 중복 데이터(동일한 아이디/이메일) 저장을 방지합니다.
    private String username;
    @Column(length = 300)
    private String password;
    @Column(unique = true)
    private String email;

    //[ 4. 시간 자동 기록 설정]
    @CreationTimestamp // 데이터가 DB에 최초로 저장(INSERT)될 때의 현재 시간을 자동으로 기록합니다.
    private Timestamp createdAt;

    // [ 5. 생성자 레벨 애노테이션 ]
    //  @Builder: 파라미터 순서에 구애받지 않고 안전하게 객체를 생성할 수 있는 빌더 패턴을 만들어줍니다.
    // -> (중요) 클래스 전체가 아닌 '특정 생성자 위'에 붙인 이유:
    //    DB가 자동으로 채워주는 값(id, createdAt)은 외부에서 조작하지 못하게 제외하고,
    //    가입 시 반드시 필요한 값(username, password, email)만 받아서 안전하게 객체를 생성하도록 강제하기 위함.
    @Builder
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }


}
