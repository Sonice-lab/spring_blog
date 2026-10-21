package com.tenco.spring_blog.user;


import com.tenco.spring_blog._core.error.UnAuthorizedException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigInteger;
import java.security.SecureRandom;

@Slf4j
@RequiredArgsConstructor
@Service
public class NaverLoginService {

    private final NaverApiClient naverApiClient;
    private final UserService userService;

    // 네이버 로그인 주소 반환 (state 생성 및 세션 저장 포함)
    public String getAuthorizeUrl(HttpSession session) {
        String state = generateState(); // 세션에 state 저장(사이트 간 위조 요청 방지)
        session.setAttribute("naverState", state);
        return naverApiClient.getAuthorizeUrl(state);
    }

    // 네이버 로그인: 인가 코드 + state -> 토큰 -> 사용자 정보 -> 가입 또는 로그인
    public SessionUserDto login(String code, String state, HttpSession session) {
        if(code == null) {
            throw new UnAuthorizedException("네이버 로그인이 취소되었습니다.");
        }

        // state 검증(사이트 간 요청 위조 방지)
        // todo - 이해하지 못했음
        // null 값 방지를 위해 "naverState"는 이름표 역할을 함 -> 실제로는 랜덤으로 값 생성됨
        String sessionState = (String) session.getAttribute("naverState");

        log.info("1.네이버가 돌려준 state 값: {}", state);
        log.info("2.우리 서버 세션에 저장된 state 값: {}", sessionState);

        if(sessionState == null || !sessionState.equals(state)) {
            throw new UnAuthorizedException("유효하지 않은 네이버 로그인 접근입니다. (state 불일치)");
        }
        // 사용 완료된 state는 세션에서 제거
        session.removeAttribute("naverState");
        try{

            // 1. 인가 코드로 엑세스 토큰 발급(네이버 서버 통신)
            UserResponse.OauthToken oAuthToken = naverApiClient.requestAccessToken(code, state);

            // 2. 엑세스 토큰으로 사용자 정보 조회(네이버 서버 통신)
            UserResponse.NaverUserInfo naverUserInfo = naverApiClient.requestProfile(oAuthToken.getAccessToken());

            // 3. 자동 회원가입 또는 로그인(UserService에 위임)
            return userService.naverLogin(naverUserInfo);

        } catch (HttpClientErrorException e) {
            // 네이버 4xx 응답을 한 경우 -> 인가 코드 재사용, 키 오류 등
            log.warn("네이버 API 오류: {}", e.getResponseBodyAsString());
            throw new UnAuthorizedException("네이버 로그인에 실패했습니다. 로그인을 재시도 하세요.");
        } catch (ResourceAccessException e) {
            // 네이버 서버에 연결하지 못한 경우 -> 네트워크 오류, 시간 초과
            log.warn("네이버 서버에 연결 실패: {}", e.getMessage());
            throw new UnAuthorizedException("네이버 서버에 연결할 수 없습니다. 잠시 후 로그인을 다시 시도해주세요.");
        }
    }


    // 난수 생성 헬퍼 메서드 (CSRF(사이트 간 요청 위조) 방지용 state 생성)
    // 암호학적으로 안전한 무작위(Random) 문자열(토큰)을 생성하는 코드
    private String generateState() {
        SecureRandom random = new SecureRandom();
        // 130비트 크기의 매우 큰 무작위 정수 객체
        // 만들어진 거대한 숫자를 32진법 문자열로 변환
        return new BigInteger(130, random).toString(32);
    }
}
