package com.tenco.spring_blog.board;

import com.tenco.spring_blog.user.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Optional;

@DataJpaTest
public class BoardJpaRepositoryTest {

    @Autowired
    private BoardJpaRepository boardJpaRepository;

    @Autowired
    private EntityManager em;

    private User saveUniqueUser(String prefix) {
        String uniqueName = prefix + "_" + System.nanoTime();
        User user = User.builder()
                .username(uniqueName)
                .password("1234")
                .email(uniqueName + "@nate.com")
                .build();
        em.persist(user);
        return user;
    }

    private Board createBoard(String title, String content, User user) {
        return Board.builder()
                .title(title)
                .content(content)
                .user(user)
                .build();
    }

    private void clearAllBoardsSafely() {
        try {
            em.createQuery("DELETE FROM Reply").executeUpdate();
        } catch (Exception ignored) {
        }
        em.createQuery("DELETE FROM Board").executeUpdate();
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("findByIdJoinUser - 조건이 맞는 게시글 ID로 조회 테스트")
    void findByIdJoinUser_존재하는_게시글_테스트() {
        // given: 테스트용 작성자와 게시글을 직접 저장하고 영속성 컨텍스트를 초기화한다
        User user = saveUniqueUser("ssar");
        Board savedBoard = boardJpaRepository.save(createBoard("테스트 제목", "테스트 내용", user));
        em.flush();
        em.clear();

        // when: 방금 저장한 게시글의 ID로 조회한다
        Optional<Board> result = boardJpaRepository.findByIdJoinUser(savedBoard.getId());

        // then: 게시글이 정상 조회되고 제목이 일치하는지 확인한다
        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getTitle()).isEqualTo("테스트 제목");
    }

    @Test
    @DisplayName("findByIdJoinUser - JOIN FETCH로 작성자(user)가 함께 로딩되는지 테스트")
    void findByIdJoinUser_작성자_페치조인_로딩_테스트() {
        // given: 작성자와 게시글을 저장한 뒤 1차 캐시를 완전히 비운다
        User user = saveUniqueUser("ssar");
        Board savedBoard = boardJpaRepository.save(createBoard("페치조인 제목", "페치조인 내용", user));
        em.flush();
        em.clear();

        // when: findByIdJoinUser 메서드로 게시글을 조회한다
        Optional<Board> result = boardJpaRepository.findByIdJoinUser(savedBoard.getId());

        // then: PersistenceUnitUtil.isLoaded로 user 필드가 프록시가 아닌 실제 객체로 로딩되었는지 확인한다
        PersistenceUnitUtil util = em.getEntityManagerFactory().getPersistenceUnitUtil();
        Assertions.assertThat(util.isLoaded(result.orElseThrow(), "user")).isTrue();
    }

    @Test
    @DisplayName("findByIdJoinUser - 조건이 맞지 않는 ID 조회 시 빈 Optional 반환 테스트")
    void findByIdJoinUser_존재하지않는_게시글_테스트() {
        // given: 게시글 1건을 저장한다
        User user = saveUniqueUser("ssar");
        boardJpaRepository.save(createBoard("제목1", "내용1", user));
        em.flush();
        em.clear();

        // when: 절대 존재하지 않는 ID(999999L)로 조회한다
        Optional<Board> result = boardJpaRepository.findByIdJoinUser(999999L);

        // then: 결과가 비어있는지(Optional.empty) 확인한다
        Assertions.assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByIdJoinUser - 테이블에 데이터가 없을 때 결과 없음 테스트")
    void findByIdJoinUser_결과없음_테스트() {
        // given: 모든 게시글 데이터를 안전하게 비운다
        clearAllBoardsSafely();

        // when: 빈 테이블에서 ID 1L을 조회한다
        Optional<Board> result = boardJpaRepository.findByIdJoinUser(1L);

        // then: 조회 결과가 없는지 확인한다
        Assertions.assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAllJoinUser - 페이징 크기, 전체 개수, 전체 페이지 수 확인 테스트")
    void findAllJoinUser_페이징_메타데이터_테스트() {
        // given: 기존 데이터를 비우고 테스트용 게시글 5건을 새로 저장한다
        clearAllBoardsSafely();
        User user = saveUniqueUser("ssar");
        for (int i = 1; i <= 5; i++) {
            boardJpaRepository.save(createBoard("제목" + i, "내용" + i, user));
        }
        em.flush();
        em.clear();
        Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "id"));

        // when: 페이지 크기 3으로 0번째 페이지를 조회한다
        Page<Board> result = boardJpaRepository.findAllJoinUser(pageable);

        // then: 현재 페이지 크기(3), 전체 개수(5), 전체 페이지 수(2)가 정확한지 확인한다
        Assertions.assertThat(result.getContent()).hasSize(3);
        Assertions.assertThat(result.getTotalElements()).isEqualTo(5);
        Assertions.assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("findAllJoinUser - ID 내림차순 정렬 순서 확인 테스트")
    void findAllJoinUser_정렬순서_테스트() {
        // given: 게시글 2건을 순서대로 저장한다
        User user = saveUniqueUser("ssar");
        boardJpaRepository.save(createBoard("먼저_작성한_글", "내용1", user));
        boardJpaRepository.save(createBoard("나중에_작성한_글", "내용2", user));
        em.flush();
        em.clear();
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"));

        // when: id 내림차순(DESC) 조건으로 첫 페이지를 조회한다
        Page<Board> result = boardJpaRepository.findAllJoinUser(pageable);

        // then: 가장 마지막에 저장한 "나중에_작성한_글"이 맨 앞(0번 인덱스)에 오는지 확인한다
        Assertions.assertThat(result.getContent().get(0).getTitle()).isEqualTo("나중에_작성한_글");
    }

    @Test
    @DisplayName("findAllJoinUser - 페이징 조회 시 작성자(user) 페치 조인 확인 테스트")
    void findAllJoinUser_작성자_페치조인_로딩_테스트() {
        // given: 게시글 1건을 저장하고 영속성 컨텍스트를 비운다
        User user = saveUniqueUser("ssar");
        boardJpaRepository.save(createBoard("페치조인 확인용", "내용", user));
        em.flush();
        em.clear();
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"));

        // when: 페이징 목록을 조회한다
        Page<Board> result = boardJpaRepository.findAllJoinUser(pageable);

        // then: 조회된 게시글의 user 엔티티가 함께 로딩되어 있는지 확인한다
        PersistenceUnitUtil util = em.getEntityManagerFactory().getPersistenceUnitUtil();
        Assertions.assertThat(util.isLoaded(result.getContent().get(0), "user")).isTrue();
    }

    @Test
    @DisplayName("findAllJoinUser - 데이터가 없을 때 빈 페이지 반환 테스트")
    void findAllJoinUser_결과없음_테스트() {
        // given: 테이블의 모든 게시글을 비운다
        clearAllBoardsSafely();
        Pageable pageable = PageRequest.of(0, 5);

        // when: 빈 테이블을 페이징 조회한다
        Page<Board> result = boardJpaRepository.findAllJoinUser(pageable);

        // then: 조회된 리스트가 비어있고 전체 개수가 0인지 확인한다
        Assertions.assertThat(result.getContent()).isEmpty();
        Assertions.assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("findAllKeywordJoinUser - 제목만 일치하는 키워드 검색 테스트")
    void findAllKeywordJoinUser_제목만_일치_테스트() {
        // given: 제목에만 고유 키워드("TITLE_ONLY_KEY")가 들어간 게시글과 무관한 게시글을 저장한다
        User user = saveUniqueUser("ssar");
        boardJpaRepository.save(createBoard("특별한 TITLE_ONLY_KEY 제목", "평범한 내용", user));
        boardJpaRepository.save(createBoard("평범한 제목", "평범한 내용", user));
        em.flush();
        em.clear();
        Pageable pageable = PageRequest.of(0, 5);

        // when: "TITLE_ONLY_KEY"로 검색한다
        Page<Board> result = boardJpaRepository.findAllKeywordJoinUser(pageable, "TITLE_ONLY_KEY");

        // then: 제목이 일치하는 1건만 조회되는지 확인한다
        Assertions.assertThat(result.getContent()).hasSize(1);
        Assertions.assertThat(result.getContent().get(0).getTitle()).contains("TITLE_ONLY_KEY");
    }

    @Test
    @DisplayName("findAllKeywordJoinUser - 내용만 일치하는 키워드 검색 테스트")
    void findAllKeywordJoinUser_내용만_일치_테스트() {
        // given: 내용에만 고유 키워드("CONTENT_ONLY_KEY")가 들어간 게시글을 저장한다
        User user = saveUniqueUser("ssar");
        boardJpaRepository.save(createBoard("평범한 제목", "특별한 CONTENT_ONLY_KEY 내용", user));
        boardJpaRepository.save(createBoard("평범한 제목2", "평범한 내용2", user));
        em.flush();
        em.clear();
        Pageable pageable = PageRequest.of(0, 5);

        // when: "CONTENT_ONLY_KEY"로 검색한다
        Page<Board> result = boardJpaRepository.findAllKeywordJoinUser(pageable, "CONTENT_ONLY_KEY");

        // then: 내용이 일치하는 1건만 조회되는지 확인한다
        Assertions.assertThat(result.getContent()).hasSize(1);
        Assertions.assertThat(result.getContent().get(0).getContent()).contains("CONTENT_ONLY_KEY");
    }

    @Test
    @DisplayName("findAllKeywordJoinUser - 제목과 내용 둘 다 불일치하는 경우 테스트")
    void findAllKeywordJoinUser_둘다_불일치_테스트() {
        // given: 일반 게시글을 저장한다
        User user = saveUniqueUser("ssar");
        boardJpaRepository.save(createBoard("스프링 부트", "백엔드 개발", user));
        em.flush();
        em.clear();
        Pageable pageable = PageRequest.of(0, 5);

        // when: 어떤 게시글에도 없는 키워드("NEVER_MATCH_KEYWORD_999")로 검색한다
        Page<Board> result = boardJpaRepository.findAllKeywordJoinUser(pageable, "NEVER_MATCH_KEYWORD_999");

        // then: 검색 결과가 비어있는지 확인한다
        Assertions.assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findAllKeywordJoinUser - 빈 키워드 검색 시 전체 게시글 조회 테스트")
    void findAllKeywordJoinUser_빈_키워드_테스트() {
        // given: 현재 게시글 수에 테스트용 게시글 2건을 추가 저장한다
        long initialCount = boardJpaRepository.count();
        User user = saveUniqueUser("ssar");
        boardJpaRepository.save(createBoard("빈키워드제목1", "내용1", user));
        boardJpaRepository.save(createBoard("빈키워드제목2", "내용2", user));
        em.flush();
        em.clear();
        Pageable pageable = PageRequest.of(0, 100);

        // when: 빈 문자열("")을 키워드로 넣어 검색한다
        Page<Board> result = boardJpaRepository.findAllKeywordJoinUser(pageable, "");

        // then: 전체 데이터 개수가 (기존 개수 + 2)와 일치하는지 확인한다
        Assertions.assertThat(result.getTotalElements()).isEqualTo(initialCount + 2);
    }

    @Test
    @DisplayName("findAllKeywordJoinUser - 검색 결과의 작성자(user) 페치 조인 로딩 테스트")
    void findAllKeywordJoinUser_작성자_페치조인_로딩_테스트() {
        // given: 고유 키워드가 포함된 게시글을 저장하고 1차 캐시를 비운다
        User user = saveUniqueUser("ssar");
        boardJpaRepository.save(createBoard("FETCH_KEY_TITLE", "내용", user));
        em.flush();
        em.clear();
        Pageable pageable = PageRequest.of(0, 5);

        // when: "FETCH_KEY_TITLE" 키워드로 검색한다
        Page<Board> result = boardJpaRepository.findAllKeywordJoinUser(pageable, "FETCH_KEY_TITLE");

        // then: 검색된 게시글의 user 연관 엔티티가 즉시 로딩되었는지 확인한다
        PersistenceUnitUtil util = em.getEntityManagerFactory().getPersistenceUnitUtil();
        Assertions.assertThat(util.isLoaded(result.getContent().get(0), "user")).isTrue();
    }
}