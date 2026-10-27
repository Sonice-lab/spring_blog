package com.tenco.spring_blog.payment;

import com.tenco.spring_blog._core.error.Exception400;
import lombok.Data;

public class PaymentRequest {

    // 1단계: 결제 건 생성 요청 {amount : 5000}
    @Data
    public static class PrepareDto {
        private Integer amount;

        public void validate() {
            if(amount == null || amount < 1000) {
                throw new Exception400("최소 충전 금액은 1,000포인트입니다.");
            }
            if(amount > 100000) {
                throw new Exception400("최대 충전 금액은 100,000원입니다.");
            }
        }
    }
    // 3단계: 결제 검증 요청(2단계는 포트원에서 처리)
    // 응답은 {paymentId: "point_...."}
    @Data
    public static class CompleteDto {
        private String paymentId;

        public void validate() {
            if(paymentId == null || paymentId.isBlank()) {
                // 결제 관련 처리를 하려고 하는데 핵심 값인 paymentId(결제 번호)가 넘어오지 않았어(비어있음)
                // 유효증 검증 관련 에러 메세지
                throw new Exception400("결제 건 식별자가 필요합니다.");
            }
        }
    }

}
