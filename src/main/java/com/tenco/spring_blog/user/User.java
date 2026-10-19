package com.tenco.spring_blog.user;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.sql.Timestamp;

@Getter
@NoArgsConstructor // 기본 생성자 필수(JPA 엔티티 생성자)
@AllArgsConstructor
@Table(name="user_tb")
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



    // 편의성을 위한 생성자 만들기
    // id와 createdAt은 자동으로 채워지므로 빌더에서 제외
    @Builder
    public User(String username, String password, String email, String profileImage) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.profileImage  = profileImage;
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
}
