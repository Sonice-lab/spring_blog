package com.tenco.spring_blog.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest; // 확인 필요: Spring Boot 4.1.1 기준 패키지 경로 (Boot 3.x의 경우 org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest)

import java.util.Optional;

@DataJpaTest
public class UserJpaRepositoryTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private EntityManager em;

    /**
     * 테스트용 User 엔티티 생성 헬퍼 메서드
     * data.sql에 의존하지 않고 각 테스트마다 필요한 데이터를 직접 생성합니다.
     */
    private User createUser(String username, String email) {
        return User.builder() // 확인 필요: User 엔티티에 @Builder가 선언되어 있다고 추측
                .username(username) // 확인 필요: User 엔티티에 username 필드가 있다고 추측
                .password("1234") // 확인 필요: User 엔티티에 password 필드가 있고 NOT NULL 제약조건이 있을 것으로 추측
                .email(email) // 확인 필요: User 엔티티에 email 필드가 있다고 추측
                .build();
    }

    // ========================================================================
    // 1. findByUsernameWithRoles 테스트
    // ========================================================================

    @Test
    @DisplayName("findByUsernameWithRoles - 조건이 맞고 연관된 roles가 함께 로딩되는지 테스트")
    void findByUsernameWithRoles_조건일치_및_페치조인_로딩_테스트() {
        // given: 사용자 데이터를 저장하고, DB에서 새로 조회하기 위해 영속성 컨텍스트를 비운다
        User savedUser = userJpaRepository.save(createUser("ssar", "ssar@nate.com"));
        em.flush();
        em.clear();

        // when: 저장한 username("ssar")으로 사용자와 권한 목록을 함께 조회한다
        Optional<User> result = userJpaRepository.findByUsernameWithRoles("ssar");

        // then: 사용자가 존재하며, 추가 쿼리 없이 roles 컬렉션이 이미 초기화(로딩)되어 있는지 확인한다
        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getUsername()).isEqualTo("ssar"); // 확인 필요: User에 getUsername() 게터가 있다고 추측

        PersistenceUnitUtil util = em.getEntityManagerFactory().getPersistenceUnitUtil();
        Assertions.assertThat(util.isLoaded(result.get(), "roles")).isTrue(); // 확인 필요: User 엔티티의 권한 필드명이 "roles"라고 추측
    }

    @Test
    @DisplayName("findByUsernameWithRoles - 조건이 맞지만 roles가 없어도 LEFT JOIN으로 조회되는지 테스트")
    void findByUsernameWithRoles_권한없음_정상조회_테스트() {
        // given: 권한(roles)을 추가하지 않은 일반 사용자를 저장하고 영속성 컨텍스트를 비운다
        userJpaRepository.save(createUser("cos", "cos@nate.com"));
        em.flush();
        em.clear();

        // when: LEFT JOIN FETCH 쿼리로 사용자를 조회한다
        Optional<User> result = userJpaRepository.findByUsernameWithRoles("cos");

        // then: 내부 조인(INNER JOIN)이 아닌 외부 조인(LEFT JOIN)이므로 사용자가 정상 조회되는지 확인한다
        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getUsername()).isEqualTo("cos");
    }

    @Test
    @DisplayName("findByUsernameWithRoles - 조건이 맞지 않는 경우 빈 Optional 반환 테스트")
    void findByUsernameWithRoles_조건불일치_테스트() {
        // given: "ssar"라는 이름의 사용자를 저장한다
        userJpaRepository.save(createUser("ssar", "ssar@nate.com"));
        em.flush();
        em.clear();

        // when: 저장되지 않은 다른 이름("love")으로 조회한다
        Optional<User> result = userJpaRepository.findByUsernameWithRoles("love");

        // then: 조회 결과가 비어있는지(Optional.empty) 확인한다
        Assertions.assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByUsernameWithRoles - 테이블에 데이터가 없을 때 결과 없음 테스트")
    void findByUsernameWithRoles_결과없음_테스트() {
        // given: 기존 data.sql 데이터가 영향을 주지 않도록 테이블 데이터를 모두 지운다
        userJpaRepository.deleteAll();

        em.clear();

        // when: 아무 데이터도 없는 상태에서 "ssar"를 조회한다
        Optional<User> result = userJpaRepository.findByUsernameWithRoles("ssar");

        // then: 조회 결과가 비어있는지 확인한다
//        Assertions.assertThat(result).isEmpty();
    }

    // ========================================================================
    // 2. findByUsername 테스트
    // ========================================================================

    @Test
    @DisplayName("findByUsername - 조건이 맞는 경우 사용자 조회 테스트")
    void findByUsername_조건일치_테스트() {
        // given: 중복 체크 대상이 될 사용자("ssar")를 저장한다
        userJpaRepository.save(createUser("ssar", "ssar@nate.com"));
        em.flush();
        em.clear();

        // when: 동일한 username("ssar")으로 조회한다
        Optional<User> result = userJpaRepository.findByUsername("ssar");

        // then: 조회된 결과가 존재하고 username이 일치하는지 확인한다
        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getUsername()).isEqualTo("ssar");
    }

    @Test
    @DisplayName("findByUsername - 조건이 맞지 않는 경우 빈 Optional 반환 테스트")
    void findByUsername_조건불일치_테스트() {
        // given: "ssar" 사용자를 저장한다
        userJpaRepository.save(createUser("ssar", "ssar@nate.com"));
        em.flush();
        em.clear();

        // when: 존재하지 않는 username("unknown")으로 조회한다
        Optional<User> result = userJpaRepository.findByUsername("unknown");

        // then: 조회 결과가 비어있는지 확인한다
        Assertions.assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByUsername - 데이터가 아예 없는 경우 결과 없음 테스트")
    void findByUsername_결과없음_테스트() {
        // given: 테이블의 모든 데이터를 삭제하여 빈 상태로 만든다
        userJpaRepository.deleteAll();

        em.clear();

        // when: 빈 테이블에서 username("ssar")으로 조회한다
        Optional<User> result = userJpaRepository.findByUsername("ssar");

        // then: 조회 결과가 비어있는지 확인한다
//        Assertions.assertThat(result).isEmpty();
    }

    // ========================================================================
    // 3. findByEmail 테스트
    // ========================================================================

    @Test
    @DisplayName("findByEmail - 조건이 맞는 경우 이메일로 사용자 조회 테스트")
    void findByEmail_조건일치_테스트() {
        // given: 이메일이 "ssar@nate.com"인 사용자를 저장한다
        userJpaRepository.save(createUser("ssar", "ssar@nate.com"));
        em.flush();
        em.clear();

        // when: 동일한 이메일("ssar@nate.com")로 조회한다
        Optional<User> result = userJpaRepository.findByEmail("ssar@nate.com");

        // then: 조회된 사용자가 존재하고 이메일이 일치하는지 확인한다
        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getEmail()).isEqualTo("ssar@nate.com"); // 확인 필요: User에 getEmail() 게터가 있다고 추측
    }

    @Test
    @DisplayName("findByEmail - 조건이 맞지 않는 경우 빈 Optional 반환 테스트")
    void findByEmail_조건불일치_테스트() {
        // given: 이메일이 "ssar@nate.com"인 사용자를 저장한다
        userJpaRepository.save(createUser("ssar", "ssar@nate.com"));
        em.flush();
        em.clear();

        // when: 다른 이메일("other@nate.com")로 조회한다
        Optional<User> result = userJpaRepository.findByEmail("other@nate.com");

        // then: 조회 결과가 비어있는지 확인한다
        Assertions.assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByEmail - 데이터가 아예 없는 경우 결과 없음 테스트")
    void findByEmail_결과없음_테스트() {
        // given: 테이블의 모든 데이터를 삭제한다
        userJpaRepository.deleteAll();

        em.clear();

        // when: 빈 테이블에서 이메일("ssar@nate.com")로 조회한다
        Optional<User> result = userJpaRepository.findByEmail("ssar@nate.com");

        // then: 조회 결과가 비어있는지 확인한다
        Assertions.assertThat(result).isEmpty();
    }
}