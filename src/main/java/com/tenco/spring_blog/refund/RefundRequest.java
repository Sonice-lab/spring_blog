package com.tenco.spring_blog.refund;

import com.tenco.spring_blog._core.error.Exception400;
import lombok.Data;

public class RefundRequest {

    @Data
    public static class SaveDto {
        private Long paymentPk; // 결제 내역 아이디(pk)
        private String reason; // 환불사유

        public void validate() {
            if (paymentPk == null) {
                throw new Exception400("결제 정보가 없습니다.");
            }

            if (reason == null || reason.trim().isEmpty()) {
                throw new Exception400("환불 사유는 필수입니다.");
            }

            // todo - 생각해보기 500자가 넘으면 아예 타이핑 자체가 안되도록 변환
            if (reason.length() > 500) {
                throw new Exception400("환불 사유는 500자 이하로 입력해주세요.");
            }
        }

    }

}
