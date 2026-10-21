package com.tenco.spring_blog.user;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j // 네이버 서버와 HTTP 통신만 담당하는 클래스
@Component // IoC 싱글톤
public class NaverApiClient {

    // application.yaml의 네이버 변수를 가지로 올 때 사용
    @Value("${oauth.naver.client-id}")
    private String naverClientId;

    @Value("${oauth.naver.client-secret}")
    private String clientSecret;

    @Value("${oauth.naver.redirect-uri}")
    private String redirectUri;

    // 인가 요청 주소
    private static String AUTHORIZE_URL = "https://nid.naver.com/oauth2.0/authorize";

    // 엑세스 토큰 요청 주소
    private static String TOKEN_URL = "https://nid.naver.com/oauth2.0/token";

    // 프로필 이미지 및 정보 요청 주소
    private static String PROFILE_URL = "https://openapi.naver.com/v1/nid/me";

    // 선언과 동시에 생성 -> 한번 만들어두면 여러 요청에서 함께 써도 안전하게 설계되기 때문에
    // 하나만 만들어 재사용하기
    private final RestTemplate restTemplate = new RestTemplate();

    // 1. 네이버 로그인 화면 주소 만들기 (로그인 + 동의 항목 화면 + state 포함)
    public String getAuthorizeUrl(String state) {
        return AUTHORIZE_URL
                + "?response_type=code"
                + "&client_id=" + naverClientId
                + "&redirect_uri=" + redirectUri
                + "&state=" + state;
    }

    // 2. 인가 코드와 state로 엑세스 토큰 요청 (POST, 폼데이터 형식으로 전달)
    public UserResponse.OauthToken requestAccessToken(String code, String state) {
        // 받은 인가 코드와 state를 활용해서 토큰 요청(POST)\
        // 가. 헤더 만들기
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 나. 본문 부분 데이터 준비
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", naverClientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("state", state);// 네이버는 검증값이 필수로 포함되어야 함

        // 다. 헤더와 바디를 합쳐주는 메서드
        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, headers);

        // 라. 주소, method, HTTP 메세지, 응답 타입
        ResponseEntity<UserResponse.OauthToken> response = restTemplate.exchange(
                TOKEN_URL,
                HttpMethod.POST,
                tokenRequest,
                UserResponse.OauthToken.class
        );
        log.info("네이버 엑세스 토큰 발급 완료");
        return response.getBody();
    }

    // 3. 엑세스 토큰으로 사용자 정보 요청(GET, 인증 헤더)
    public UserResponse.NaverUserInfo requestProfile(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth((accessToken)); // 인증 헤더 설정

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<UserResponse.NaverUserInfo> response = restTemplate.exchange(
                PROFILE_URL,
                HttpMethod.GET,
                request,
                UserResponse.NaverUserInfo.class
        );
        return response.getBody();
    }
}
