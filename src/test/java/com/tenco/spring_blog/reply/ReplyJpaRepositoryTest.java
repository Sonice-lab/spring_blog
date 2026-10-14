package com.tenco.spring_blog.reply;

import com.tenco.spring_blog.board.Board;
import com.tenco.spring_blog.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ReplyJpaRepositoryTest {

    @Autowired
    private ReplyJpaRepository replyJpaRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("특정 게시글의 댓글 목록과 작성자를 오름차순으로 함께 조회한다")
    void findAllByBoardIdJoinUser_test() {
        // given (데이터 준비)
        User user1 = User.builder().username("user1").password("1234").build();
        User user2 = User.builder().username("user2").password("1234").build();
        em.persist(user1);
        em.persist(user2);

        Board board1 = Board.builder().title("제목1").content("내용1").user(user1).build();
        Board board2 = Board.builder().title("제목2").content("내용2").user(user2).build();
        em.persist(board1);
        em.persist(board2);

        // board1에 댓글 2개 (작성자 각각 다르게)
        Reply reply1 = Reply.builder().comment("댓글1").user(user1).board(board1).build();
        Reply reply2 = Reply.builder().comment("댓글2").user(user2).board(board1).build();
        // board2에 댓글 1개
        Reply reply3 = Reply.builder().comment("댓글3").user(user1).board(board2).build();

        em.persist(reply1);
        em.persist(reply2);
        em.persist(reply3);

        // DB에 반영하고 영속성 컨텍스트(1차 캐시)를 비움
        // 이렇게 해야 fetch join 쿼리가 실제로 DB로 날아가는지 테스트 가능
        em.flush();
        em.clear();

        // when (실행)
        List<Reply> replies = replyJpaRepository.findAllByBoardIdJoinUser(board1.getId());

        // then (검증)
        // 1. 갯수 확인 (board1의 댓글은 2개여야 함)
        assertThat(replies).hasSize(2);

        // 2. 오름차순 정렬 확인 (id가 낮은 것이 먼저 와야 함)
        assertThat(replies.get(0).getComment()).isEqualTo("댓글1");
        assertThat(replies.get(1).getComment()).isEqualTo("댓글2");

        // 3. fetch join 확인 (LazyInitializationException 없이 User 정보가 담겨있는지 확인)
        assertThat(replies.get(0).getUser().getUsername()).isEqualTo("user1");
        assertThat(replies.get(1).getUser().getUsername()).isEqualTo("user2");
    }

}
