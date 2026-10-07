package com.tenco.spring_blog.board;


import com.tenco.spring_blog.user.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import({BoardPersistRepository.class})
@DataJpaTest
public class BoardPersistRepositoryTest {

    // 삭제 테스트 코드
    @Test
    public void deleteByID_게시글_삭제_테스트() {
        // given
        // 1. 삭제를 위해 먼저 테스트용 게시글을 생성하고 저장합니다.
        User user = new User(1L, "testuser", "1234", "a@naver.com", null);
        Board board = Board.builder()
                .title("삭제 테스트글")
                .content("삭제 테스트내용")
                .user(user)
                .build();

        Board savedBoard = boardPersistRepository.save(board);
        Long targetId = savedBoard.getId(); // 삭제할 게시글의 ID

        // when
        // 2. 저장된 게시글의 ID를 이용해 삭제 메서드를 호출합니다.
        boardPersistRepository.deleteByID(targetId);

        // then
        // 3. 영속성 컨텍스트(1차 캐시)에서 삭제되었으므로, 다시 조회하면 null이 반환되어야 합니다.
        Board deletedBoard = boardPersistRepository.findById(targetId);
        Assertions.assertThat(deletedBoard).isNull();
    }

    @Test
    public void deleteByID_존재하지_않는_게시글_삭제_예외_테스트() {
        // given
        // DB에 존재하지 않을 임의의 ID를 설정합니다.
        Long nonExistentId = 999L;

        // when & then
        // 존재하지 않는 ID로 삭제를 시도할 때 IllegalArgumentException이 발생하는지 검증합니다.
        Assertions.assertThatThrownBy(() -> boardPersistRepository.deleteByID(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("삭제할 게시글을 찾을 수 없습니다.");
    }

    @Autowired
    private BoardPersistRepository boardPersistRepository;

    @Test
    public void save_연관관계_포함_게시글_저장_테스트() {
        //given
        // 1. User 객체 생성 (실제로는 세션에서 가져온다.)
        User user = new User(1L, "testuser", "1234", "a@naver.com", null);

        // 2. Board 엔티티
        Board board = Board.builder()
                .title("테스트글")
                .content("테스트내용")
                .user(user)
                .build();

        //when
        Board savedBoard = boardPersistRepository.save(board);

        //then
        // 1. 자동 생성된 ID값 확인
        Assertions.assertThat(savedBoard.getId()).isNotNull();
        Assertions.assertThat(savedBoard.getId()).isGreaterThan(0);

        // 2. 입력한 데이터가 올바르게 저장되었는지 확인
        Assertions.assertThat(savedBoard.getTitle()).isEqualTo("테스트글");
        Assertions.assertThat(savedBoard.getContent()).isEqualTo("테스트내용");

        // 3. 연관관계가 올바르게 저장되었는지 확인
        Assertions.assertThat(savedBoard.getUser()).isNotNull();
        Assertions.assertThat(savedBoard.getUser().getUsername()).isEqualTo("testuser");

        // 4. 원본 객체와 반환된 객체가 동일한 참조인지 확인
        Assertions.assertThat(board).isSameAs(savedBoard);

    }
}
