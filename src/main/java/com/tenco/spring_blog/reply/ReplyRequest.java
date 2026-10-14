package com.tenco.spring_blog.reply;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog.board.Board;
import com.tenco.spring_blog.user.User;
import lombok.Data;

public class ReplyRequest {

    // 댓글 정보를 통째로 가져오기 위한 DTO 설계
    // 댓글 작성  Dto
    @Data
    public static class SaveDto {
        // 고민1: 화면으로 보았을 때 명확하게 어떤 것이 나와야하는 지 알 수 있으나,
        // 어떤 게시글에 대한 댓글인지를 명시해야한다.
        private Long boardId; // 댓글을 달 게시글 id(화면에서는 hidden input 전달)
        private String comment;

        // 엉망으로 작성된 부분이 요청되는 것을 방지
        public void validate() {
            if (boardId == null) {
                throw new Exception400("게시글 정보가 없습니다.");
            }

            if (comment == null || comment.trim().isEmpty()) {
                throw new Exception400("댓글 내용을 입력해주세요.");
            }

            if (comment.length() > 500) {
                throw new Exception400("댓글은 500자 이하로 입력해주세요.");
            }
        }

        // 편의 메서드 구현
        // toEntity
        public Reply toEntity(Board board, User user) {
            return Reply.builder()
                    .comment(this.comment)
                    .board(board)
                    .user(user)
                    .build();
        }
    }
}
