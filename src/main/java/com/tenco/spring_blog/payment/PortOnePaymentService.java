package com.tenco.spring_blog.payment;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception500;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

@Slf4j
@RequiredArgsConstructor
@Service
public class PortOnePaymentService {

    private final PortOneApiClient portOneApiClient;
    private final PaymentService paymentService;

    public PaymentResponse.PrepareDto prepare(Long sessionUserId, Integer amount) {
        String paymentId = paymentService.prepare(sessionUserId, amount);

        return PaymentResponse.PrepareDto.builder()
                .paymentId(paymentId)
                .amount(amount)
                .storeId(portOneApiClient.getStoreId())
                .channelKey(portOneApiClient.getChannelKey())
                .build();

    }

    // (중요)3단계: 포트원 조회(트랜잭션 밖) -> 검증(포트원 서버) -> 충전(트랜잭션 안)
    public PaymentResponse.CompleteDto complete(Long sessionUserId, String paymentId) {
        PaymentResponse.PortOnePayment portOnePayment;

        // 1. 포트원 서버에 실제 결제 조회 (외부 통신)
        try {
            portOnePayment = portOneApiClient.getPayment(paymentId);
            // 여기서 대기
        } catch (HttpClientErrorException e) {
            // 포트원 4xx 응답 (없는 결제 건, API 시크릿 키 오류)
            throw new Exception400("결제 건이 확인되지 않습니다.");
        } catch (ResourceAccessException e) {
            // 포트 원 서버 연결 실패 (네트워크 오류, 시간 초과 등)
            throw new Exception500("결제 서버에 연결할 수 없습니다.");
        }


        // null 여부관련 방어적 코드 왜 체크해야하는가? -> 새로운 객체가 안 만들어지거나, 한 번더 검증 차 확인
        if (portOnePayment == null) {
            throw new Exception400("결제 정보를 확인할 수 없습니다.");
        }

        // 2. 검증(포트원 응답 데이터, 결제 DB 정보로 비교 검증) -> 검증이 맞다면 포인트 충전 -> 내부 트랜잭션은 분리 처리 되었다.
        return paymentService.complete(sessionUserId, paymentId, portOnePayment);
    }


}
