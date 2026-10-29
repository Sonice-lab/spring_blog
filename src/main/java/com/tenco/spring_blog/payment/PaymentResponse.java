package com.tenco.spring_blog.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tenco.spring_blog._core.util.MyDateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

public class PaymentResponse {

    // 결제 내역(포인트 결제) DTO
    @Data
    public static class ListDto {
        private Long id; // 결제 내역 pk
        private String paymentId;
        private String pgTxId;
        private int amount;
        private String statusDisplay; // 상태 표시 이름 (결제 완료 등)
        private String paidAt; // 결제 시간
        private boolean cancelled; // 추가: 환불 완료 결제 인지확인 (배지 색 구분용)
        private boolean refundable; // 추가: 환불 요청 버튼 표시 여부

        // todo - 환불 테이블 만들기
        // 환불 가능 여부는 환불 요청 기록이 있어야 알 수 있으므로
        // 서비스에서 계산하여 넘겨준다.
        @Builder
        public ListDto(Payment payment, boolean refundable) {
            this.id = payment.getId();
            this.paymentId = payment.getPaymentId();
            this.pgTxId = payment.getPgTxId();
            this.amount = payment.getAmount();
            this.statusDisplay = payment.getStatus().getDisplayName(); //결제 완료, 결제 대기, 결제 취소 등
            this.paidAt = MyDateUtil.timestampFormat(payment.getCreatedAt());
            this.cancelled = payment.getStatus() == PaymentStatus.CANCELLED; // 같다면 true, 아니면 false
            this.refundable = refundable;
        }
    }


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
