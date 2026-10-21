package com.tenco.spring_blog.user;


import com.tenco.spring_blog._core.error.UnAuthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoLoginService {

    private final KakaoApiClient kakaoApiClient; // HTTP 통신 목적 컨트롤 x
    private final UserService userService;

    // 카카오 로그인 주소 반환
    // 카카오 로그인 반환 주소
    public String getAuthorizeUrl() {
        return kakaoApiClient.getAuthorizeUrl();
    }

    // 카카오 로그인: 인가 코드 -> 토큰 -> 사용자 정보 -> 가입 또는 로그인
    public SessionUserDto login(String code) {
        if (code == null) {
            throw new UnAuthorizedException("카카오 로그인이 취소되었습니다.");
        }

        try {
            // 1. 인가 코드로 엑세스 토큰 발급 (카카오 서버 통신)
            UserResponse.OauthToken oAuthToken = kakaoApiClient.requestAccessToken(code);

            // 2. 엑세스 토큰으로 사용자 정보 조회(카카오 서버 통신)
            UserResponse.KakaoUserInfo kakaoUserInfo = kakaoApiClient.requestProfile(oAuthToken.getAccessToken());

            // 3. 자동 회원가입 또는 로그인 (우리 DB의 트랜잭션을 USERSERVICE에서 시킬 예정)
            return userService.kakaoLogin(kakaoUserInfo);


        } catch (HttpClientErrorException e) {
            // 카카오 4xx 응답을 한 경우 -> 인가 코드 재사용, 키 오류 등
            log.warn("카카오 API 오류: {}", e.getResponseBodyAsString());
            throw new UnAuthorizedException("카카오 로그인에 실패했습니다. 로그인을 재시도 하세요.");
        } catch (ResourceAccessException e) {
            // 실제 카카오 DB자원 서버로, 카카오 서버에 연결하지 못한 경우 -> 네트워크 오류, 시간 초과
            log.warn("카카오 서버에 연결 실패: {}", e.getMessage());
            throw new UnAuthorizedException("카카오 서버에 연결할 수 없습니다. 잠시 후 다시 시도하세요.");
        }

    }

}
