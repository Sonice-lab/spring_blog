package com.tenco.spring_blog.refund;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog.payment.Payment;
import com.tenco.spring_blog.payment.PaymentJpaRepository;
import com.tenco.spring_blog.payment.PaymentResponse;
import com.tenco.spring_blog.payment.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentJpaRepository paymentJpaRepository;

    /**
     * 환불 요청 폼에 보여줄 결제 정보
     *
     * @param paymentPk
     * @param userId
     */
    public PaymentResponse.ListDto findRequestForm(Long paymentPk, Long userId) {
        // 환불 요청이 가능 여부 확인
        Payment payment = checkRefundable(paymentPk, userId);
        // 응답 정상 동작하면 응답 DTO 생성 후 리턴
        // 환불 가능한 Payment 엔티티만 조회되었기 때문에 무조건 true 처리
        return new PaymentResponse.ListDto(payment, true);

    }

    private Payment checkRefundable(Long paymentPk, Long userId) {

        // select를 일으킨다. - PAID 상태인가?
        // 1. payment 테이블에 결제 내역 조회

        Payment payment = paymentJpaRepository.findById(paymentPk)
                .orElseThrow(() -> new Exception400("결제 내역을 찾을 수 없습니다."));

        // 2. 본인이 결제한 내역인지 확인
        if (!payment.isOwner(userId)) {
            throw new Exception403("권한 없음! 본인이 한 결제 내역만 환불받으실 수 있습니다.");
        }

        // 3. 추가 결제 완료 상태만 환불 가능 (READY, CANCELLED는 환불 불가)
        if (!payment.isPaid()) {
            throw new Exception400("결제가 완료된 건만 환불 요청하실 수 있습니다.");
        }

        // 4. 환불 테이블에서 이미 환불이 진행된 요청인지 확인한다.(대기중, 승인됨, 거절됨의 경우는 신청 불가)
        // 이미 환불 요청한 결제인지 확인
        if (refundRepository.findByPaymentPk(paymentPk).isPresent()) {
            throw new Exception400("이미 환불 요청이 진행된 건입니다.");
        }
        return payment;
    }

    /**
     * 환불 요청 저장
     */
    @Transactional
    public void save(RefundRequest.SaveDto saveDto, Long userId) {
        // 1. 폼 요청 화면처럼 검사를 한 번 더 실행 (주소 직접 요청 방지, 중복 제출 방지)
        Payment payment = checkRefundable(saveDto.getPaymentPk(), userId);

        // 2. 환불 요청 저장
        Refund refund = Refund.builder()
                .user(payment.getUser())
                .payment(payment)
                .reason(saveDto.getReason().trim())
                .build();

        refundRepository.save(refund);
    }

    // 환불 요청한 리스트 불러오기(혼자 실습한 것)
    public List<RefundResponse.ListDto> findAllByUserId(Long userId) {

       List<Refund> refundList = refundRepository.findAllByUserIdJoinPayment(userId);

        // 리스트로 반환
        return refundList.stream()
                .map(RefundResponse.ListDto::new)
                .toList();

    }
}
