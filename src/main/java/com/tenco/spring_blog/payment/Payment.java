package com.tenco.spring_blog.payment;

// 상태
// 1. 결제 예정
// 2. 결제 완료

import com.tenco.spring_blog.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

// 결제 내역 엔티티 (포트원 v2)
// -paymentId: 우리 서버가 발급한 결제 건 식별자 (유니크, 중복 지급 방지)
// -amount: 결제 예정 금액 (실제로 결제 금액은 포트원에서 처리 -> 응답 -> 실제 결제 금액과 우리 사이트 결제 금액 비교)
@Getter
@NoArgsConstructor
@Entity
@Table(name = "payment_tb")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 결제 건 식별자(같은 값이 두번 저장되지 않도록 유니크 처리)
    @Column(unique = true, nullable = false, length = 100)
    private String paymentId;

    // PG 사 거래번호 (결제 완료되면 - 포트원 사이트에서 응답을 받음, 추후 추적용)
    private String pgTxId;

    // 결제한 사용자 - 한 유저는 여러 번 결제할 수 있다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 결제 금액(충전할 포인트)
    @Column(nullable = false) // not null
    private int amount;

    // 이 결제 건이 예정인지 완료인지의 유무(결제 대기, 환불, 완료 등의 상태)
    @Enumerated(EnumType.STRING) // Enum 타입을 데이터베이스(MySQL 등)가 이해할 수 있는 형태(문자열이나 숫자)로 변환해서 저장하기 위해서 어노테이션 처리
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    // 결제 시간
    @CreationTimestamp
    private Timestamp createdAt;

    // 생성자
    @Builder
    public Payment(String paymentId, User user, int amount) {
        this.paymentId = paymentId;
        this.user = user;
        this.amount = amount;
        this.status = PaymentStatus.READY; //처음에는 항상 결제 예정으로 설정된다.(무통장입금 등)
    }

    // 편의메서드 설계 - 결제를 요청한 사용자인가?

    // 결제를 요청한 사용자인지 확인
    public boolean isOwner(Long userId) {
        return this.user.getId().equals(userId);
    }

    // 이미 처리(포인트 지급 완료)된 결제인지 확인
    public boolean isPaid() {
        return this.status == PaymentStatus.PAID;
    }

    // 추후 - 검증 통과 후 결제 완료로 변경 처리 (더티 체킹)
    public void markPaid(String pgTxId){
        this.status = PaymentStatus.PAID; //결제 상태를 완료로 변경
        this.pgTxId = pgTxId; // null 값 허용
    }

}
