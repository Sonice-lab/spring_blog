package com.tenco.spring_blog.purchase;

import com.tenco.spring_blog._core.util.MyDateUtil;
import lombok.Builder;
import lombok.Data;

public class PurchaseResponse {

    // 구매 내역 항목 1개의 클래스
    @Data
    public static class ListDto {
        private Long id; // 구매 내역 pk
        private Long boardId; // 게시글 PK (상세보기 링크용)
        private String boardTitle; // 게시글 제목
        private String boardUsername; // 게시글 작성자 이름
        private int price; // 구매 가격
        private String purchasedAt; // 구매일 (포맷된 문자열)

        @Builder
        public ListDto(Purchase purchase) {
            this.id = purchase.getId();
            if (purchase.getBoard() != null) {
                this.boardId = purchase.getBoard().getId();
                this.boardTitle = purchase.getBoard().getTitle();
                this.boardUsername = purchase.getBoard().getUser().getUsername();
            }
            this.price = purchase.getPrice();
            this.purchasedAt = MyDateUtil.timestampFormat(purchase.getCreatedAt());
        }
    }

}
