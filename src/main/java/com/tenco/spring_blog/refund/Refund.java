package com.tenco.spring_blog.refund;


import com.tenco.spring_blog.payment.Payment;
import com.tenco.spring_blog.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "refund_tb")
public class Refund {

    // 어떤 요소들이 들어와야할까? (잠시 생각해보기)
    // 사용자의 입장 - 주문번호? 결제번호, 거래사 거래승인번호, 결제일, 결제금액, 결제 상태, 환불 요청 가능, 환불 요청 현황 여부
    // 관리자의 입장 - 식별 번호,환불 신청자, 결제번호, 환불금액, 요청일, 소비자의 환불 요청 상태 관련(대기, 승인, 거절 등)
    // 관리자의 입장에서 상세 페이지에 들어갔을 때는 어떤 멤버 변수가 필요할까? -
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //환불을 요청한 사용자(User 1: Refund N)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 비즈니스 로직: 결제 한 건에 환불 요청 1건만 허용
    // 예시) 결제한 제품들의 부분 환불의 상황일 경우
    // 혹시 - 대표가 부분 환불을 해달라고 할 경우 @ManyToOne -> 1건의 환불 건에서 부분환불을 가질 수 있다.
    // 현재 환불 정책(결제 1건에 환불 요청 1건) + unique
    // 나중에라도 부분 환불로 해야될 경우 unique 만 제거 + @ManyToone만해도 비즈니스 규칙에는 영향을 받지 않는다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    // 사용자가 입력한 환불 사유
    @Column(nullable = false, length = 500)
    private String reason;

    // 환불 상태: 최초 무조건(대기) 관리자 판단에 따라서 승인, 거절로 업데이트 된다. (더디체킹 메서드로 구현 todo)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefundStatus status;

    // 관리자 거절사유: 거절사유가 있다면 화면에 출력(거절할 경우만 저장)
    @Column(length = 500)
    private String rejectReason;

    // 환불 요청 시각 - 생성 시 자동으로 시각을 넣어준다.
    @CreationTimestamp
    private Timestamp createdAt;

    // 업데이트 시각 - 업데이트 시 자동으로 시각을 넣어준다.
    @UpdateTimestamp
    private Timestamp updatedAt; //마지막 변경 시각 (승인, 거절)

    @Builder
    public Refund(User user, Payment payment, String reason) {
        this.user = user;
        this.payment = payment;
        this.reason = reason;
        this.status = RefundStatus.PENDING;
    }

    // 편의 메서드

    // 1. 관리자 기준 - 관리자가 아직 처리하지 않은 요청인지 확인
    public boolean isPending() {
        return this.status == RefundStatus.PENDING;
    }

    // 승인 처리(트랜잭션 안에서 호출하면 더티 체킹으로 UPDATE 처리)
    public void approve() {
        this.status = RefundStatus.APPROVED;

    }

    // 거절 + 거절 사유
    public void reject(String rejectReason) {
        this.status = RefundStatus.REJECTED;
        this.rejectReason = rejectReason;

    }




}
