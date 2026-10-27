package com.tenco.spring_blog.payment;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;


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


}
