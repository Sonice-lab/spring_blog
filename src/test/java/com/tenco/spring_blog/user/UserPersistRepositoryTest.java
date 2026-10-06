package com.tenco.spring_blog.user;

import lombok.Data;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

// 본 파일은 UserPersistRepository 테스트 하기 위함

@Import(UserPersistRepository.class)
@DataJpaTest // JPA테스트에 필요한 환경으로 자동으로 구성해줌
public class UserPersistRepositoryTest {

    @Autowired // 상수가 아닌 부분에 대한 DI 처리 -> 잘 안씀 -> 상수가 성능이 더 빠름 -> 가능한한 상수로 설계하기
    private UserPersistRepository userPersistRepository;

    @Test
    public void findByUsernameAndPassword_로그인_성공_테스트() {

        // given : 회원가입된 사용자 정보
        User user = User.builder()
                .username("testUser")
                .password("3456")
                .email("test@email.com")
                .build();

        userPersistRepository.save(user);

        // when : 사용자명과 비밀번호로 조회
        User loginUser = userPersistRepository.findByUsernameAndPassword(
                "testUser",
                "3456"
        );

        // then : 조회된 사용자 검증
        Assertions.assertThat(loginUser).isNotNull();
        Assertions.assertThat(loginUser.getId()).isNotNull();
        Assertions.assertThat(loginUser.getUsername()).isEqualTo("testUser");
        Assertions.assertThat(loginUser.getPassword()).isEqualTo("3456");
        Assertions.assertThat(loginUser.getEmail()).isEqualTo("test@email.com");

        // 저장한 객체와 조회한 객체가 같은 엔티티인지 확인
        Assertions.assertThat(loginUser).isSameAs(user);
    }


    @Test
    public void findByUsernameAndPassword_비밀번호_불일치_테스트() {

        // given : 회원가입된 사용자 정보
        User user = User.builder()
                .username("testUser")
                .password("3456")
                .email("test@email.com")
                .build();

        userPersistRepository.save(user);

        // when : 잘못된 비밀번호로 로그인 시도
        User loginUser = userPersistRepository.findByUsernameAndPassword(
                "testUser",
                "9999"
        );

        // then : 로그인 실패 -> null
        Assertions.assertThat(loginUser).isNull();
    }


    @Test
    public void findByUsernameAndPassword_존재하지않는_사용자_테스트() {

        // given : DB에 존재하지 않는 사용자 정보
        String username = "xxxxx";
        String password = "3456";

        // when : 존재하지 않는 사용자로 로그인 시도
        User loginUser = userPersistRepository.findByUsernameAndPassword(
                username,
                password
        );

        // then : 로그인 실패 -> null
        Assertions.assertThat(loginUser).isNull();
    }

    // 단위 테스트 할 메서드 설계하기
    // 내가 작성한 코드가 제대로 돌아가는가? JUnit
    @Test
    public void save_회원가입_테스트() {
        // 3가지 패턴이 있음
        // given: 회원가입 시 사용자 정보
        User user = User
                .builder()
                .username("testUser")
                .password("3456")
                .email("test@email.com")
                .build();

        // 저장 전 상태 확인: ID는 저장전에 NULL이어야 함
        Assertions.assertThat(user.getId()).isNull();
        System.out.println("저장 전 User: " + user);

        // when: 회원가입 실행 시 테스트(영속화되어있어야 함)
        User savedUser = userPersistRepository.save(user);

        // then: 저장된 결과를 검증할 때 코드를 작성
        // 1. 자동 생성된 ID 값 확인
        Assertions.assertThat(savedUser.getId()).isNotNull();
        Assertions.assertThat(savedUser.getId()).isGreaterThan(0);

        // 참고) 테스트 코드는 LLM을 활용하는 것이 효율적임!
        // 2. 입력한 데이터가 올바르게 저장되었는지 확인
        Assertions.assertThat(savedUser.getCreatedAt()).isNotNull();
        Assertions.assertThat(savedUser.getUsername()).isEqualTo("testUser");
        Assertions.assertThat(savedUser.getPassword()).isEqualTo("3456");
        Assertions.assertThat(savedUser.getEmail()).isEqualTo("test@email.com");

        // 3. 원본 객체와 반환된 객체가 동일한 참조인지 확인
        // 영속성 컨텍스트는 같은 엔티티에 대해 같은 인스턴스를 보장한다.
        Assertions.assertThat(user).isSameAs(savedUser); //true, false 반환

    }

    @Test
    public void findByUsername_존재하지않는_사용자_테스트() {
        //given : 존재하지 않은 사용자명
        String username = "xxxxx";

        //when
        User notFoundUser = userPersistRepository.findByUserName(username);

        //then: null 반환 확인
        Assertions.assertThat(notFoundUser).isNull();


    }







}
