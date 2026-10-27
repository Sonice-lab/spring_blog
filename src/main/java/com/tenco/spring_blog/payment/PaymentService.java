package com.tenco.spring_blog.payment;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog.user.User;
import com.tenco.spring_blog.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentJpaRepository paymentJpaRepository;
    private final UserJpaRepository userJpaRepository;

    /**
     * 1단계: 결제 건 생성
     * paymentId를 발급하고, "누가 얼마를 결제할 예정인지 READY 상태로 테이블에 저장"
     */
    @Transactional
    public String prepare(Long sessionUserId, Integer amount) {
        User user = userJpaRepository.findById(sessionUserId)
                .orElseThrow(() -> new Exception400("사용자를 찾을 수 없습니다."));

        // 식별자가 겹치면 다시 만들기
        String paymentId = generatePaymentId(sessionUserId);

        // 조회 기능 만들기 관련 처리 -> 중복되지 않는 paymentId 생성 완료
        while(paymentJpaRepository.existsByPaymentId(paymentId)) {
            paymentId = generatePaymentId(sessionUserId);
        }

        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .user(user)
                .amount(amount)
                .build();

        paymentJpaRepository.save(payment);

        return paymentId;
    }

    // 방어적 코드 - 중복있는 부분 확인 - 테이블에 있는 데이터를 조회 -> 중복이 있다면 다시 생성하도록 함
    private static String generatePaymentId(Long userId) {
        String paymentId = "point_" + userId + "_"
                + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
        return paymentId;
    }

    // 3단계 : 결제 검증 + 포인트 충전
    // 포트원에서 조회한 결과를 1단계 기록 payment_tb 정보랑 비교하기
    // 하나라도 맞지 않으면 예외 --> 트랜잭션 롤백(포인트 지급 안 됨)
    @Transactional
    public PaymentResponse.CompleteDto complete(Long sessionUserId, String paymentId, PaymentResponse.PortOnePayment portOnePayment) {
        // 1. 우리 서버가 발급한 결제 건인지를 확인
        Payment payment = paymentJpaRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new Exception400("결제 정보를 찾을 수 없습니다."));

        // 2. 결제를 시작한 본인인지 확인
        if(!payment.isOwner(sessionUserId)) {
            throw new Exception403("본인의 결제 내역만 처리할 수 있습니다.");
        }

        // 3. 중복 지급 방지: 이미 PAID이면 거부
        if(payment.isPaid()) {
            throw new Exception400("이미 처리된 결제입니다.");
        }

        // 4. [중요] 포트원 기준 결제 상태 확인
        if (!"PAID".equals(portOnePayment.getStatus())) {
            throw new Exception400("결제가 완료되지 않았습니다.: " + portOnePayment.getStatus());
        }

        // 5. [중요] 금액 위변조 확인: 실제 결제 금액 == 1단계에서 기록한 금액이 동일해야함
        if(portOnePayment.getAmount() == null
                || portOnePayment.getAmount().getTotal() == null
                || portOnePayment.getAmount().getTotal() != payment.getAmount()) {

            throw new Exception400("결제 금액이 일치하지 않습니다.");
        }

        // 6. 검증 통과: 포인트 충전 + 결제 완료 표시 (더디 체킹)
        User user = payment.getUser();
        user.chargePoint(payment.getAmount()); // 해당 유저의 포인트 충전 됨
        payment.markPaid(portOnePayment.getPgTxId());

        log.info("결제 완료 및 포인트 충전 - 사용자 ID: {}, paymentId: {}, 금액: {}",
                sessionUserId, paymentId, payment.getAmount());

        return new PaymentResponse.CompleteDto(payment.getAmount(), user.getPoint());

    }
}