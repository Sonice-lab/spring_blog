package com.tenco.spring_blog.user;

import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

// 카카오 서버와 HTTP 통신만 담당하는 클래스
@Slf4j
@Component // IoC (싱글톤)
public class KakaoApiClient {

    //application.yaml의 변수를 가지고 올 때 사용
    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth.kakao.client-secret}")
    private String clientSecret;

    @Value("${oauth.kakao.redirect-uri}")
    private String redirectUri;

    @Value("${oauth.kakao.access-token}")
    private String accessToken;

    // 인가 요청 주소
    private static String AUTHORIZE_URL = "https://kauth.kakao.com/oauth/authorize";

    // 엑세스 토큰 요청 주소
    private static String TOKEN_URL = "https://kauth.kakao.com/oauth/token";

    // 프로필 이미지 요청 주소
    private static String PROFILE_URL = "https://kapi.kakao.com/v2/user/me";

    // 선언과 동시에 생성 -> 한번 만들어두면 여러 요청에서 함께 써도 안전하게 설계되기 때문에
    // 하나만 만들어 재사용한다.
    private final RestTemplate restTemplate = new RestTemplate();

    // 1. 카카오 로그인 화면 주소 만들기(로그인 + 동의 항목 화면)
    public String getAuthorizeUrl() {
        return AUTHORIZE_URL
                + "?client_id=" + kakaoClientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code";
    }

    // 2. 인가 코드로 엑세스 토큰 요청 (POST, 폼데이터 형식으로 전달)
    public UserResponse.OauthToken requestAccessToken(String code) {

        // 1. 받은 인가코드로 활용해서 토큰 요청(POST)
        // 가. 헤더 만들기
        HttpHeaders Headers = new HttpHeaders();
        // Content-Type: application/x-www-form-urlencoded;charset=utf-8
        Headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 나. 본문 부분 데이터 준비
        MultiValueMap<String, String> Params = new LinkedMultiValueMap<>();
        Params.add("grant_type", "authorization_code");
        Params.add("client_id", kakaoClientId);
        Params.add("redirect_uri", redirectUri);
        Params.add("code", code);
        // 클라이언트 시크릿 활성화 시 반드시 포함해주어야 한다.(필수가 아니더라도)
        Params.add("client_secret", clientSecret);


        // 다. 헤더와 바디를 합쳐주는 메서드
        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity(Params, Headers);

        // 라. 주소, method, HTTP 메세지, 응답 타입
        ResponseEntity<UserResponse.OauthToken> response = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                tokenRequest,
                UserResponse.OauthToken.class  //Gson을 사용할 필요 없음
        );
        log.info("카카오 엑세스 토큰 발급 완료");  // 토큰값은 가능한 한 로그에 남기지 않는다.
        return response.getBody();
    }

    // 3. 엑세스 토큰으로 사용자 정보 요청(GET/POST 가능하지만 GET으로 활용, 인증 헤더)
    public UserResponse.KakaoUserInfo requestProfile(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken); // 여기까지 헤더 완성

        HttpEntity request = new HttpEntity<>(headers);

        ResponseEntity<UserResponse.KakaoUserInfo> response = restTemplate.exchange(
                PROFILE_URL,
                HttpMethod.GET,
                request,
                UserResponse.KakaoUserInfo.class
        );
        return response.getBody();
    }

}
