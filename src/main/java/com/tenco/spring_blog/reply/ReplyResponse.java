package com.tenco.spring_blog.reply;

import com.tenco.spring_blog._core.util.MyDateUtil;
import lombok.Data;

public class ReplyResponse {

    // 댓글 목록 응답 DTO
    @Data
    public static class ListDto {

        // 댓글을 불러오기 위해서는 작성자Id(pk), 작성자명, 댓글내용, 포맷된 시간, 내 댓글 여부
        // 필드 적어보기
        private Long id; // pk
        private String username;
        private String comment;
        private String createdAt; //포맷된 작성일
        // 세션 아이디(로그인한 아이디)와 댓글 작성자 아이디가 일치하는가?(삭제 버튼 표시용)
        private boolean isOwner;

        // 생성자
        public ListDto(Reply reply, Long sessionUserId) {
            this.id = reply.getId();
            this.comment = reply.getComment();
            this.username = reply.getUser().getUsername(); //join fetch로 바로 가져와서 활용
            if (reply.getCreatedAt() != null) {
                this.createdAt = MyDateUtil.timestampFormat(reply.getCreatedAt());
            }
            this.isOwner = reply.isOwner(sessionUserId);
        }
    }
}
