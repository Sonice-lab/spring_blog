package com.tenco.spring_blog.reply;

import com.tenco.spring_blog.board.Board;
import com.tenco.spring_blog.user.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

@DataJpaTest
public class ReplyJpaRepositoryTest {

    @Autowired
    private ReplyJpaRepository replyJpaRepository;

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

    private Board saveBoard(String title, User user) {
        Board board = Board.builder()
                .title(title)
                .content("테스트 게시글 내용")
                .user(user)
                .build();
        em.persist(board);
        return board;
    }

    private Reply createReply(String comment, Board board, User user) {
        return Reply.builder()
                .comment(comment)
                .board(board)
                .user(user)
                .build();
    }

    @Test
    @DisplayName("findAllByBoardIdJoinUser - 조건이 맞는 게시글 ID의 댓글 목록만 조회되는지 테스트")
    void findAllByBoardIdJoinUser_조건일치_테스트() {
        // given: 게시글 2개(board1, board2)를 만들고 board1에 댓글 2개, board2에 댓글 1개를 저장한다
        User user = saveUniqueUser("ssar");
        Board board1 = saveBoard("게시글1", user);
        Board board2 = saveBoard("게시글2", user);

        replyJpaRepository.save(createReply("게시글1의 첫 댓글", board1, user));
        replyJpaRepository.save(createReply("게시글1의 두 번째 댓글", board1, user));
        replyJpaRepository.save(createReply("게시글2의 댓글", board2, user));
        em.flush();
        em.clear();

        // when: board1의 ID로 댓글 목록을 조회한다
        List<Reply> result = replyJpaRepository.findAllByBoardIdJoinUser(board1.getId());

        // then: board2의 댓글은 섞이지 않고 board1의 댓글 2건만 조회되는지 확인한다
        Assertions.assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("findAllByBoardIdJoinUser - 댓글이 오래된 순(id 오름차순)으로 정렬되는지 테스트")
    void findAllByBoardIdJoinUser_정렬순서_테스트() {
        // given: 하나의 게시글에 댓글 2개를 시간 순서대로 저장한다
        User user = saveUniqueUser("ssar");
        Board board = saveBoard("게시글1", user);

        Reply firstReply = replyJpaRepository.save(createReply("먼저 작성한 댓글", board, user));
        Reply secondReply = replyJpaRepository.save(createReply("나중에 작성한 댓글", board, user));
        em.flush();
        em.clear();

        // when: 해당 게시글의 댓글 목록을 조회한다
        List<Reply> result = replyJpaRepository.findAllByBoardIdJoinUser(board.getId());

        // then: ORDER BY r.id ASC 조건에 따라 먼저 저장된 댓글의 ID가 첫 번째(0번 인덱스)에 오는지 확인한다
        Assertions.assertThat(result.get(0).getId()).isEqualTo(firstReply.getId());
        Assertions.assertThat(result.get(1).getId()).isEqualTo(secondReply.getId());
    }

    @Test
    @DisplayName("findAllByBoardIdJoinUser - JOIN FETCH로 댓글 작성자(user)가 함께 로딩되는지 테스트")
    void findAllByBoardIdJoinUser_작성자_페치조인_로딩_테스트() {
        // given: 댓글 1건을 저장한 뒤 1차 캐시(영속성 컨텍스트)를 완전히 비운다
        User user = saveUniqueUser("ssar");
        Board board = saveBoard("게시글1", user);
        replyJpaRepository.save(createReply("페치조인 확인용 댓글", board, user));
        em.flush();
        em.clear();

        // when: findAllByBoardIdJoinUser로 댓글 목록을 조회한다
        List<Reply> result = replyJpaRepository.findAllByBoardIdJoinUser(board.getId());

        // then: 추가 SELECT 쿼리 없이 Reply 내부의 user 엔티티가 이미 초기화(로딩)되었는지 확인한다
        PersistenceUnitUtil util = em.getEntityManagerFactory().getPersistenceUnitUtil();
        Assertions.assertThat(util.isLoaded(result.get(0), "user")).isTrue();
    }

    @Test
    @DisplayName("findAllByBoardIdJoinUser - 조건이 맞지 않는 게시글 ID 조회 시 빈 리스트 반환 테스트")
    void findAllByBoardIdJoinUser_조건불일치_테스트() {
        // given: 댓글이 달린 게시글(board1)과 댓글이 전혀 없는 새 게시글(emptyBoard)을 저장한다
        User user = saveUniqueUser("ssar");
        Board board1 = saveBoard("댓글 있는 게시글", user);
        Board emptyBoard = saveBoard("댓글 없는 게시글", user);
        replyJpaRepository.save(createReply("댓글 내용", board1, user));
        em.flush();
        em.clear();

        // when: 댓글이 없는 emptyBoard의 ID로 조회한다
        List<Reply> result = replyJpaRepository.findAllByBoardIdJoinUser(emptyBoard.getId());

        // then: 조회 결과가 빈 리스트인지 확인한다
        Assertions.assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAllByBoardIdJoinUser - 테이블에 댓글 데이터가 아예 없을 때 결과 없음 테스트")
    void findAllByBoardIdJoinUser_결과없음_테스트() {
        // given: 댓글 테이블의 모든 데이터를 삭제하여 빈 테이블로 만든다
        em.createQuery("DELETE FROM Reply").executeUpdate();
        em.flush();
        em.clear();

        // when: 아무 댓글도 없는 상태에서 임의의 게시글 ID(1L)로 조회한다
        List<Reply> result = replyJpaRepository.findAllByBoardIdJoinUser(1L);

        // then: 조회 결과가 빈 리스트인지 확인한다
        Assertions.assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deleteByBoardId - 특정 게시글의 댓글만 일괄 삭제되고 다른 게시글 댓글은 유지되는지 테스트")
    void deleteByBoardId_조건일치_일괄삭제_테스트() {
        // given: 두 게시글(targetBoard, otherBoard)에 각각 댓글을 저장한다
        User user = saveUniqueUser("ssar");
        Board targetBoard = saveBoard("삭제할 게시글", user);
        Board otherBoard = saveBoard("유지할 게시글", user);

        replyJpaRepository.save(createReply("삭제될 댓글 1", targetBoard, user));
        replyJpaRepository.save(createReply("삭제될 댓글 2", targetBoard, user));
        replyJpaRepository.save(createReply("유지될 댓글", otherBoard, user));
        em.flush();
        em.clear();

        // when: targetBoard의 ID로 댓글 일괄 삭제 메서드를 실행하고 영속성 컨텍스트를 비운다
        replyJpaRepository.deleteByBoardId(targetBoard.getId());
        em.flush();
        em.clear();

        // then: targetBoard의 댓글은 0건이고, otherBoard의 댓글 1건은 그대로 남아있는지 확인한다
        List<Reply> targetReplies = replyJpaRepository.findAllByBoardIdJoinUser(targetBoard.getId());
        List<Reply> otherReplies = replyJpaRepository.findAllByBoardIdJoinUser(otherBoard.getId());
        Assertions.assertThat(targetReplies).isEmpty();
        Assertions.assertThat(otherReplies).hasSize(1);
    }

    @Test
    @DisplayName("deleteByBoardId - 조건이 맞지 않는 게시글 ID로 삭제 요청 시 기존 댓글 유지 테스트")
    void deleteByBoardId_조건불일치_테스트() {
        // given: 게시글에 댓글 1건을 저장한다
        User user = saveUniqueUser("ssar");
        Board board = saveBoard("게시글", user);
        replyJpaRepository.save(createReply("소중한 댓글", board, user));
        em.flush();
        em.clear();

        // when: 존재하지 않는 게시글 ID(999999L)로 삭제를 실행한다
        replyJpaRepository.deleteByBoardId(999999L);
        em.flush();
        em.clear();

        // then: 기존 게시글의 댓글이 지워지지 않고 그대로 1건 남아있는지 확인한다
        List<Reply> remainingReplies = replyJpaRepository.findAllByBoardIdJoinUser(board.getId());
        Assertions.assertThat(remainingReplies).hasSize(1);
    }

    @Test
    @DisplayName("deleteByBoardId - 댓글이 하나도 없는 상태에서 삭제 호출 시 정상 동작 테스트")
    void deleteByBoardId_결과없음_테스트() {
        // given: 댓글 테이블을 모두 비운다
        em.createQuery("DELETE FROM Reply").executeUpdate();
        em.flush();
        em.clear();

        // when: 빈 테이블 상태에서 삭제 메서드를 호출한다
        replyJpaRepository.deleteByBoardId(1L);
        em.flush();
        em.clear();

        // then: 예외 없이 실행되고 전체 댓글 개수가 0인지 확인한다
        Assertions.assertThat(replyJpaRepository.count()).isZero();
    }
}