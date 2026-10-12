package com.tenco.spring_blog.board;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

@DataJpaTest
public class BoardPersistRepositoryTest {

    @Autowired
    private BoardJpaRepository boardPersistRepository;

    @Test
    @DisplayName("게시글 삭제가 정상적으로 수행되어야 한다")
    public void deleteById_게시글_삭제_테스트() {
        // given (테스트를 위한 임의의 게시글 생성 및 저장)
        Board board = new Board(); // Board 엔티티에 맞게 생성 방식(Builder 또는 기본 생성자+Setter)을 맞춰주세요.
        board.setTitle("테스트 제목");
        board.setContent("테스트 내용");

        Board savedBoard = boardPersistRepository.save(board);
        Long targetId = savedBoard.getId(); // 저장된 게시글의 ID 추출

        // when (삭제 메서드 호출)
        boardPersistRepository.deleteById(targetId);

        // then (삭제 후 다시 조회했을 때 데이터가 없어야 함)
        Optional<Board> deletedBoard = boardPersistRepository.findById(targetId);
        Assertions.assertThat(deletedBoard).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 게시글 ID로 삭제 시도 시 예외가 발생하지 않고 무시된다")
    public void deleteById_존재하지_않는_게시글_삭제_예외발생안함() {
        // given
        Long nonExistentId = 999L;

        // when & then
        // Spring Boot 3.0 이상에서는 존재하지 않는 ID 삭제 시 예외가 발생하지 않음을 검증
        Assertions.assertThatCode(() -> boardPersistRepository.deleteById(nonExistentId))
                .doesNotThrowAnyException();
    }
}