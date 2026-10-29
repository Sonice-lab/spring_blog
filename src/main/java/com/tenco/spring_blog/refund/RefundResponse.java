package com.tenco.spring_blog.refund;

import com.tenco.spring_blog._core.util.MyDateUtil;
import lombok.Builder;
import lombok.Data;

//환불 요청시에도 이 리스트에는 함께 사용
public class RefundResponse {

    // 결제번호, 결제금액, 결제일(결제시간)
    @Data
    public static class ListDto {
        private Long id; // 환불 요청 PK
        private int amount; // 환불 요청 금액
        private String paymentId; // 포트원 결제 건 식별자(주문번호)
        private String reason;   // 본인이 작성한 환불 사유
        private String rejectReason; // 거절 사유(거절됨일 때만 값이 존재 - 관리자, null값을 가질 수 있음)
        private String statusDisplay; // 대기중, 승인됨, 거절됨
        private String requestAt; // 환불 요청일

        // 머스태치 조건 분기용 (상태별 뱃지 색상)
        private boolean pending;
        private boolean approved;
        private boolean rejected;


        @Builder
        public ListDto(Refund refund) {
            this.id = refund.getId();
            this.amount = refund.getPayment().getAmount();
            this.paymentId = refund.getPayment().getPaymentId();  // 주문번호
            this.reason = refund.getReason();
            // null이 있기에 방어적코드 작성
            if(refund.getReason() != null) {
                this.rejectReason = refund.getRejectReason();
            }
            this.statusDisplay = refund.getStatus().getDisplayName(); // enum에서 끌어옴
            if(refund.getCreatedAt() != null) {
                this.requestAt = MyDateUtil.timestampFormat(refund.getCreatedAt());
            }
            this.pending = refund.getStatus() == RefundStatus.PENDING;
            this.approved = refund.getStatus() == RefundStatus.APPROVED;
            this.rejected = refund.getStatus() == RefundStatus.REJECTED;
        }
    }

}
