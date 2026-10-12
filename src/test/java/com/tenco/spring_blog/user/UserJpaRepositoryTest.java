package com.tenco.spring_blog.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

@DataJpaTest // JPA 관련 빈만 띄우고, 각 테스트가 끝나면 롤백
public class UserJpaRepositoryTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    // data.sql 의 사용자(admin, minsu 등)와 겹치지 않는 테스트용 사용자
    private User createUser(String username, String email) {
        return User.builder()
                .username(username)
                .password("1234")
                .email(email)
                .build();
    }

    // ===== save =====

    @Test
    public void save_회원가입_테스트() {
        // given : 저장할 사용자
        User user = createUser("testUser", "test@email.com");

        // when : 저장
        User savedUser = userJpaRepository.save(user);

        // then : id 와 작성일이 자동으로 채워짐
        Assertions.assertThat(savedUser.getId()).isNotNull();
        Assertions.assertThat(savedUser.getCreatedAt()).isNotNull();
        Assertions.assertThat(savedUser.getUsername()).isEqualTo("testUser");
        Assertions.assertThat(savedUser.getEmail()).isEqualTo("test@email.com");
    }

    // ===== findById =====

    @Test
    public void findById_존재하는_사용자_조회_테스트() {
        // given : 사용자 저장
        User savedUser = userJpaRepository.save(createUser("testUser", "test@email.com"));

        // when : id 로 조회
        Optional<User> found = userJpaRepository.findById(savedUser.getId());

        // then : 조회됨
        Assertions.assertThat(found).isPresent();
        Assertions.assertThat(found.get().getUsername()).isEqualTo("testUser");
    }

    @Test
    public void findById_존재하지않는_사용자_조회_테스트() {
        // given : 존재하지 않는 id
        Long notExistId = 9999L;

        // when : 조회
        Optional<User> found = userJpaRepository.findById(notExistId);

        // then : 비어 있음
        Assertions.assertThat(found).isEmpty();
    }

    // ===== findByUsernameAndPassword (로그인) =====

    @Test
    public void findByUsernameAndPassword_로그인_성공_테스트() {
        // given : 가입된 사용자
        userJpaRepository.save(createUser("testUser", "test@email.com"));

        // when : 올바른 사용자명과 비밀번호로 조회
        Optional<User> loginUser = userJpaRepository.findByUsernameAndPassword("testUser", "1234");

        // then : 조회됨
        Assertions.assertThat(loginUser).isPresent();
        Assertions.assertThat(loginUser.get().getUsername()).isEqualTo("testUser");
    }

    @Test
    public void findByUsernameAndPassword_비밀번호_불일치_테스트() {
        // given : 가입된 사용자
        userJpaRepository.save(createUser("testUser", "test@email.com"));

        // when : 틀린 비밀번호로 조회
        Optional<User> loginUser = userJpaRepository.findByUsernameAndPassword("testUser", "9999");

        // then : 비어 있음 (로그인 실패)
        Assertions.assertThat(loginUser).isEmpty();
    }

    @Test
    public void findByUsernameAndPassword_존재하지않는_사용자_테스트() {
        // given : 가입되지 않은 사용자명
        String username = "nobody";

        // when : 조회
        Optional<User> loginUser = userJpaRepository.findByUsernameAndPassword(username, "1234");

        // then : 비어 있음
        Assertions.assertThat(loginUser).isEmpty();
    }

    // ===== findByUsername (사용자명 중복 체크) =====

    @Test
    public void findByUsername_사용자명_존재_테스트() {
        // given : 가입된 사용자
        userJpaRepository.save(createUser("testUser", "test@email.com"));

        // when : 같은 사용자명으로 조회
        Optional<User> found = userJpaRepository.findByUsername("testUser");

        // then : 조회됨 (중복)
        Assertions.assertThat(found).isPresent();
    }

    @Test
    public void findByUsername_사용자명_없음_테스트() {
        // given : 가입되지 않은 사용자명
        String username = "nobody";

        // when : 조회
        Optional<User> found = userJpaRepository.findByUsername(username);

        // then : 비어 있음 (사용 가능)
        Assertions.assertThat(found).isEmpty();
    }

    // ===== findByEmail (이메일 중복 체크) =====

    @Test
    public void findByEmail_이메일_존재_테스트() {
        // given : 가입된 사용자
        userJpaRepository.save(createUser("testUser", "test@email.com"));

        // when : 같은 이메일로 조회
        Optional<User> found = userJpaRepository.findByEmail("test@email.com");

        // then : 조회됨 (중복)
        Assertions.assertThat(found).isPresent();
        Assertions.assertThat(found.get().getUsername()).isEqualTo("testUser");
    }

    @Test
    public void findByEmail_이메일_없음_테스트() {
        // given : 가입되지 않은 이메일
        String email = "nobody@email.com";

        // when : 조회
        Optional<User> found = userJpaRepository.findByEmail(email);

        // then : 비어 있음 (사용 가능)
        Assertions.assertThat(found).isEmpty();
    }

    // ===== 더티 체킹 (회원정보 수정) =====

    @Test
    public void update_비밀번호_수정_더티체킹_테스트() {
        // given : 가입된 사용자
        User savedUser = userJpaRepository.save(createUser("testUser", "test@email.com"));

        // when : 엔티티의 값만 변경 (save 를 다시 호출하지 않음)
        savedUser.update("5678");
        userJpaRepository.flush(); // 변경 감지로 UPDATE 쿼리를 지금 실행

        // then : 새 비밀번호로 로그인 조회 가능
        Optional<User> loginUser = userJpaRepository.findByUsernameAndPassword("testUser", "5678");
        Assertions.assertThat(loginUser).isPresent();
    }
}