package com.tenco.spring_blog.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

public class PaymentResponse {

    // 1단계 응답: 브라우저가 결제창을 띄울 때 사용
    @Data
    public static class PrepareDto {
        private String paymentId;      // 서버가 발급한 결제 건 식별자
        private int amount;           // 결제 요청 금액
        private String storeId;      // 포트원 상점 ID
        private String channelKey;  //포트원 채널키

        @Builder
        public PrepareDto(String paymentId, int amount, String storeId, String channelKey) {
            this.paymentId = paymentId;
            this.amount = amount;
            this.storeId = storeId;
            this.channelKey = channelKey;
        }
    }

    // 3단계 응답: 충전 결과
    @Data
    @AllArgsConstructor
    public static class CompleteDto {
        private int amount; // 충전된 포인트
        private int currentPoint; // 충전 후 보유 포인트
    }


    // 포트원 V2 기준 결제 단건 조회 응답 (필요한 필드만 사용)
    @Data
    public static class PortOnePayment {
        private String id; // paymentId
        private String status; // READY, PAID, FAILED, CANCELLED 등의 상태를 확인
        private String pgTxId; // PG사 거래번호
        private Amount amount;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Amount {
            private Integer total;
        }
    }


}
