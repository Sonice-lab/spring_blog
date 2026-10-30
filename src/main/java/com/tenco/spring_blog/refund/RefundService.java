package com.tenco.spring_blog.refund;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog.payment.Payment;
import com.tenco.spring_blog.payment.PaymentJpaRepository;
import com.tenco.spring_blog.payment.PaymentResponse;
import com.tenco.spring_blog.payment.PaymentStatus;
import com.tenco.spring_blog.user.User;
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

    public List<RefundResponse.AdminListDto> findAllForAdmin() {

        // 관리자에서 조회할 전체 리스트
        List<Refund> refundList = refundRepository.findAllJoinUserAndPayment();

        return refundList.stream()
                .map(RefundResponse.AdminListDto::new) // 메서드 참조로 생성
                .toList();
    }

    /**
     * 객체 지향 개념의 최소한 solid원칙을 지키면서 개발하는 것이 중요
     * SOLID
     * S: 단일 책임의 원칙
     * O: 개방과 폐쇄의 원칙 등....
     * 관리자: 환불 요청 거절 (포트원 통신 없음)
     *
     * @param refundId
     * @param rejectReason
     */
    @Transactional //update가 일어나기에
    public void reject(Long refundId, String rejectReason) {

        // 1. 넘겨받은 refundId 기준으로 조회부터 해야한다. 왜? 엔티티 정보를 업데이트 하기 위해(더티 체킹)
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(
                        () -> new Exception400("환불 요청을 찾을 수 없습니다."));

        // 2. 이미 승인, 거절된 요청은 굳이 다시 처리하지 않는다.
        if (!refund.isPending()) {
            throw new Exception400("대기중인 환불 요청만 거절할 수 있습니다.");
        }

        refund.reject(rejectReason); // 더티 체킹
    }


    /**
     * 관리자 승인 1단계: 승인 가능한 지 검사 (DB만 조회)
     * @param refundId
     * @return: 포트원 결제 취소에 필요한 값 리턴
     */
    public RefundResponse.ApproveDto checkApprove(Long refundId) {
        // 조회(환불 엔티티)
        Refund refund = refundRepository.findByIdJoinUserAndPayment(refundId)
                .orElseThrow(() -> new Exception404("환불 요청을 찾을 수 없습니다."));

        // 환불 상태 확인: 대기, 승인, 거절 중 현재 대기중 상태가 아니라면 포트원 통신을 필요없다.
        if (!refund.isPending()) {
            throw new Exception400("대기중인 환불 요청만 승인할 수 있습니다.");
        }

        // 결제 (PAID, READY) -> PAID 일 때만 진행 가능?
        Payment payment = refund.getPayment();
        if(!payment.isPaid()) {
            throw new Exception400("결제 완료 상태인 내역만 환불 신청이 가능합니다.");
        }

        // User가 충전한 포인트를 이미 사용했다면 돌려받을 포인트가 없어서 환불 처리를 할 수 없음
        User user = refund.getUser();
        if(user.getPoint() < payment.getAmount()) {
            throw new Exception400("포인트 잔액이 부족하여 환불을 진행할 수 없습니다.");
        }

        return new RefundResponse.ApproveDto(payment.getPaymentId(), payment.getAmount());

        // 사용자의 현재 포인트 여부도 나와야 함
    }

    /**
     * 관리자 승인 4단계: 포트원 결제 취소가 끝난 뒤 DB 반영
     * @param refundId
     */
    @Transactional
    public void approve(Long refundId) {
        // 수정 일으키기 -> 기억해야할 것은? -> 조회부터 하도록 한다.
        Refund refund = refundRepository.findByIdJoinUserAndPayment(refundId)
                .orElseThrow(() -> new Exception404("환불 요청을 찾을 수 없습니다."));

        // 1~4번 사이에서 다른 요청이 같은 건을 처리했을 수 있기 때문에 한 번 더 확인
        if(!refund.isPending()) {
            throw new Exception400("대기중인 환불 요청만 환불 승인 신청 가능합니다.");
        }
        // 더티체킹 업데이트
        Payment payment = refund.getPayment();

        refund.getUser().deductPoint(payment.getAmount());

        payment.markCancelled(); // 상태 값 변경 CANCELLED

        // 환불 승인 처리
        refund.approve();
    }
}
