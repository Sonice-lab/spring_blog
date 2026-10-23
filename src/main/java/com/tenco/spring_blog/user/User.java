package com.tenco.spring_blog.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor // 기본 생성자 필수(JPA 엔티티 생성자)
@AllArgsConstructor
@Table(name = "user_tb")
@Entity //자동 테이블 생성
@Data
public class User {

    //pk 만들기
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 같은 사용자명을 두 번 가입할 수 없도록 unique 제약 걸기(이메일)
    @Column(unique = true) // 유니크 설정
    private String username;
    @Column(length = 300) //길이를 300으로 변경
    @Setter
    private String password;
    @Column(unique = true)
    private String email;

    @CreationTimestamp // now()
    private Timestamp createdAt;

    // 프로필 이미지 파일 이름(선택 사항이므로 null 가능)

    private String profileImage;

    // [cascade, orphanRemoval]
    // 두 옵션은 부모 쪽의 @OneToMany 컬렉션에 붙인다.
    // User가 List<UserRole>을 가지고 있으므로 사용할 수 있다.
    // 회원 탈퇴를 할 경우, 쓰레기 데이터가 생기는데, 이를 한번에 정리하기 위한 설계
    // 사용자의 역할 목록(User 1:  UserRole N 단방향 설계)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", nullable = false) //fk는 자식테이블에 생성
    private List<UserRole> roles = new ArrayList<>();

    // 가입 경로를 나타냄(DB에는 LOCAL, KAKAO 문자열로 저장)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'LOCAL'") // 문자열이므로 안에 작은 따옴표 필수!
    private OAuthProvider provider;

    // 휴대폰을 저장할 컬럼 추가
    @Column(length = 20, unique = true)
    private String phoneNumber;



    // 편의성을 위한 생성자 만들기
    // id와 createdAt은 자동으로 채워지므로 빌더에서 제외
    @Builder
    public User(String username, String password, String email, String profileImage, OAuthProvider provider) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.profileImage = profileImage;
        this.provider = (provider == null) ? OAuthProvider.LOCAL : provider;
    }

    // 편의 메서드 1. 회원 정보 수정 - 더티체킹 (변경 감지용)
    public void update(String password) {

        this.password = password;
    }

    // 편의 메서드 2. 회원 정보 수정 - 프로필 이미지 변경(null 이면 삭제)
    // 트랜잭션 안에서 호출하면 더티 체킹 발생
    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    // 편의 메서드 3. 역할 추가 메서드(회원 가입 시 기본 USER 부여, 관리자는 직접 지정)
    public void addRole(Role role) {
        // 이미 가진 역할이면 추가하지 않음 (코드에서 1차 방어)
        if (hasRole(role)) {
            return;
        }
        this.roles.add(UserRole.builder().role(role).build());
    }

    // 가. 해당 역할을 가지고 있는 지 확인
    public boolean hasRole(Role role) {
        for (UserRole u : this.roles) {
            if (u.getRole() == role) {
                return true; // 찾으면 바로 true
            }
        }
        return false; // 끝까지 없으면 false
    }

    // 편의 메서드 3. 로그인한 사용자의 관리자 여부 확인 메서드
    public boolean isAdmin() {
        return hasRole(Role.ADMIN); // 고정 값: 관리자 여부인지만 확인 후 true, false 반환
    }

    // 편의 메서드 4.
    public String getProfilePath() {
        // 프로필 이미지가 없으면 null을 반환한다.
        if (this.profileImage == null) {
            return null;
        }
        // 프로필 이미지가 http로 시작한다면 image로 반환한다.
        if (this.profileImage.startsWith("http")) {
            return this.profileImage;
        }
        return "/images/" + this.profileImage;
    }
}
