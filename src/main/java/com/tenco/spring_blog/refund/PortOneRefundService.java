package com.tenco.spring_blog.refund;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog.payment.PaymentResponse;
import com.tenco.spring_blog.payment.PortOneApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

// 환불 승인 처리 순서 관리: 검사 -> 포트원 취소 -> 응답 확인 -> DB 반영
// 분리하는 이유: 트랜잭션을 걸지 않는다.: 포트원 응답을 기다리는 동안 쓰기 트랜잭션을 열어두지 않기 위해
// DB 작업은 트랜잭션이 있는 RefundService에 맡긴다.
@Slf4j
@RequiredArgsConstructor
@Service
public class PortOneRefundService {

    private final RefundService refundService;
    private final PortOneApiClient portOneApiClient;


    public void approve(Long refundId) {
        // 1. 환불 처리 승인이 가능한 사안인가에 대한 여부 검사 (DB 조회만 진행)
        RefundResponse.ApproveDto approveDto = refundService.checkApprove(refundId);

        // 2. 포트원 결제 취소 요청 (외부 통신, 트랜잭션 밖)
        PaymentResponse.PortOneCancel portOneCancel;
        try {
            portOneCancel = portOneApiClient.cancelPayment(approveDto.getPaymentId(), approveDto.getAmount());
        } catch (RestClientResponseException e) {
            // 포트원이 오류 상태 코드로 응답 (4xx)
            // 400번대(400, 401, 403, 404, 409) 예외
            throw new RuntimeException(e);
        } catch (RestClientException e) {
            // 500번대(502) - 응답을 받지 못함 (연결 실패, 시간 초과 등)
            throw new RuntimeException(e);
        }

        // 3. 혹시 중간에 환불 취소가 확인되었을 경우 이에 대한 상황 확인
        // 포트원 취소 완료 상태 확인 (SUCCEEDED DB를 바꾸지 않는다.)
        if(portOneCancel == null
                || portOneCancel.getCancellation() == null
                || !"SUCCEEDED".equals(portOneCancel.getCancellation().getStatus()) ) {
            throw new Exception400("결제 취소가 완료되지 않았습니다. 잠시 후 포트원 콘솔에서 확인해주세요.");
        }

        // 4. 응답 받은 후 DB 반영
        // 포인트 차감해주기
        // 결제 테이블에 상태를 CANCELLED를 바꿔줘야 함
        // 환불 테이블에 상태를 APPROVED로 바꿔준다.
        refundService.approve(refundId);


    }
}
