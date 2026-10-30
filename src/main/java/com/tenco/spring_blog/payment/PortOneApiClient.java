package com.tenco.spring_blog.payment;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


//portone:
//store-id: ${PORTONE_STORE_ID:}
//channel-key: ${PORTONE_CHANNEL_KEY:}
//api-secret: ${PORTONE_API_SECRET:}


// 포트원 서버와 HTTP 통신만 담당하는 클래스(포트원 설정값도 이곳에서만 관리)
@Slf4j
@Component
@Getter
public class PortOneApiClient {

    @Value("${portone.store-id}")
    private String storeId;

    @Value("${portone.channel-key}")
    private String channelKey;

    @Value("${portone.api-secret}")
    private String apiSecret;

    private static final String PAYMENT_URL = "https://api.portone.io/payments";

    // 여러 요청에서 함께 써도 안전하므로 하나만 만들어 재사용
    private final RestTemplate restTemplate = new RestTemplate();

    // 포트원 서버에 결제 단 건 조회(GET, 헤더 설정 ) -> 문서 분석 중요
    public PaymentResponse.PortOnePayment getPayment(String paymentId) {
        HttpHeaders headers = new HttpHeaders();
        // (중요!) PortOne 작성시 반드시 공백 한 칸!
        headers.set(HttpHeaders.AUTHORIZATION, "PortOne " + apiSecret);

        // GET 요청이라 바디 없이 헤더만 담는다.
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<PaymentResponse.PortOnePayment> response = restTemplate.exchange(
                PAYMENT_URL + "/" + paymentId,
                HttpMethod.GET,
                request,
                PaymentResponse.PortOnePayment.class
        );
        log.info("포트원 결제 조회 완료: {}", response);
        return response.getBody();
    }

    // 포트원 서버에 결제 취소 요청
    // amount를 넣으면 그 금액만큼 취소됨(부분 환불 가능), 넣지 않을 경우 전액 취소(사전에 문서에서 확인해야하는 부분)
    // 이번 로직은 결제 금액 전체를 넣는다.
    /**
     * POST /payments/point_2_1761030000000_a1b2c3d4/cancel HTTP/1.1
     * Host: api.portone.io
     * Authorization: PortOne ${PORTONE_API_SECRET}
     * Content-Type: application/json
     */
    public PaymentResponse.PortOneCancel cancelPayment(String paymentId, int amount) {

        // 헤더 생성
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "PortOne " + apiSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 본문 부분 나올 데이터 준비
       Map<String, Object> body = Map.of("amount", amount, "reason", "관리자 환불 승인");

        // 바디와 헤더를 합쳐주는 메서드
        HttpEntity<Map<String,Object>> request = new HttpEntity(body, headers);

        // 주소, method, HTTP 메세지, 응답 타입
        ResponseEntity<PaymentResponse.PortOneCancel> response = restTemplate.exchange(
                PAYMENT_URL + "/" + paymentId + "/cancel",
                HttpMethod.POST,
                request,
                PaymentResponse.PortOneCancel.class
        );

        return response.getBody();

    }
}
