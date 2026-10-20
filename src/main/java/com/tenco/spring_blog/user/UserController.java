package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.UnAuthorizedException;
import com.tenco.spring_blog._core.util.Define;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor //DI 처리
@Slf4j
@Controller //IoC (제어의 역전) 싱글톤 패턴으로 관리됨
public class UserController {

    private final UserService userService;

    //application.yaml의 변수를 가지고 올 때 사용
    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth.kakao.client-secret}")
    private String clientSecret;

    @Value("${oauth.kakao.redirect-uri}")
    private String redirectUri;

    @Value("${oauth.kakao.access-token}")
    private String accessToken;




    @GetMapping("/kakao-login")
    public String kakaoLogin() {
        // 리다이렉트 주소 설계
        String url = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + kakaoClientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code";
        return "redirect:" + url;
    }

    // 흐름
    // 1. 인가 코드 받기 --> 엑세스 토큰 요청(보안상의 이유) -> 사용자 정보 요청 -> 가입 또는 로그인 처리
    @GetMapping("/oauth/kakao")
    public String kakaoCallback(@RequestParam(name = "code", required = false) String code,
                                HttpSession session) {
        // 방어적 코드
        if (code == null) {
            throw new UnAuthorizedException("카카오 로그인이 취소되었습니다.");
        }

        //
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<UserResponse.OauthToken> tokenResponse; // 다운캐스팅 없이 바로 사용할 수 있음
        ResponseEntity<UserResponse.KakaoUserInfo> profileResponse;

        try {

            // 1. 받은 인가코드로 활용해서 토큰 요청(POST)
            // 가. 헤더 만들기
            HttpHeaders tokenHeaders = new HttpHeaders();
            // Content-Type: application/x-www-form-urlencoded;charset=utf-8
            tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 나. 본문 부분 데이터 준비
            MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
            tokenParams.add("grant_type", "authorization_code");
            tokenParams.add("client_id", kakaoClientId);
            tokenParams.add("redirect_uri", redirectUri);
            tokenParams.add("code", code);
            // 클라이언트 시크릿 활성화 시 반드시 포함해주어야 한다.(필수가 아니더라도)
            tokenParams.add("client_secret", clientSecret);


            // 다. 헤더와 바디를 합쳐주는 메서드
            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity(tokenParams, tokenHeaders);

            // 라. 주소, method, HTTP 메세지, 응답 타입
            tokenResponse = restTemplate.exchange(
                    "https://kauth.kakao.com/oauth/token",
                    HttpMethod.POST,
                    tokenRequest,
                    UserResponse.OauthToken.class  //Gson을 사용할 필요 없음
            );

            // 엑세스 토큰으로 사용자 정보 요청(카카오 서버)
            // 헤더 만들기
            HttpHeaders profileHeaders = new HttpHeaders();
            profileHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            profileHeaders.setBearerAuth(tokenResponse.getBody().getAccessToken());

            // 헤더 + 엔티티
            HttpEntity profileRequest = new HttpEntity(profileHeaders);

            //-> HTTP 통신 요청하기
            profileResponse = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    profileRequest,
                    UserResponse.KakaoUserInfo.class
            );

            UserResponse.KakaoUserInfo  kakaoUserInfo = profileResponse.getBody();
            // 내부 로직 - 최초 소셜 로그인 사용자라면 자동 회원 가입(필요 정보가 더 있다면 추가 기입)
            //          - 이미 회원가입 되어있는 사용자라면 로그인 처리
            SessionUserDto sessionUserDto = userService.kakaoLogin(kakaoUserInfo);
            session.setAttribute(Define.SESSION_USER, sessionUserDto);

            return "redirect:/";

        } catch (HttpClientErrorException e) {
            // 카카오가 4xx를 응답한 경우 오류 발생
            log.error("카카오 API 오류: {}" + e.getResponseBodyAsString());
            throw new UnAuthorizedException("카카오 로그인에 실패했습니다.");
        }


    }


    // 프로필 삭제
    @PostMapping("/user/profile-image/delete")
    public String deleteProfileImage(HttpSession session) {
        // 아무나 삭제할 수 없도록 인가 처리가 필요
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        userService.deleteProfileImage(sessionUserDto.getId());
        return "redirect:/user/detail";
    }


    // 주소 설계
    // GET http://localhost:8080/user/detail
    @GetMapping("/user/detail")
    public String detail(Model model, HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        // 서비스 단에 위임
        UserResponse.DetailDto user = userService.findDetail(sessionUserDto.getId());
        model.addAttribute("user", user);
        return "user/detail";
    }

    // 주소 설계
    // GET http://localhost:8080/join
    @GetMapping("/join")
    public String joinForm() {
        return "user/join-form";
    }

    // POST http://localhost:8080/join
    @PostMapping("/join")
    //파싱 전략
    public String join(UserRequest.JoinDto joinDto, Model model) {

        // 기능 코드 추가
        // 인증 검사(이전에 로그인을 했는가?) -> 필요 없음 / 유효성 검사 -> 필요 -> 실시
        joinDto.Validate();
        userService.join(joinDto);
        return "redirect:/login";
    }

    // 주소 설계
    // GET http://localhost:8080/login
    @GetMapping("/login")
    public String loginForm() {
        return "user/login-form";
    }

    // POST http://localhost:8080/login
    // 로그인 처리(예외적으로 POST 요청(보안상의 이유))
    @PostMapping("/login")
    public String login(UserRequest.LoginDto loginDto, HttpSession session, Model model) {

        // 입력데이터 검증 -> 유효성 검사
        loginDto.Validate();
        SessionUserDto sessionUserDto = userService.login(loginDto);

        // 보안상의 이유로 비밀번호 정보는 저장하고 싶지 않은 경우 -> 있어야 함
        session.setAttribute(Define.SESSION_USER, sessionUserDto);

        // 성공시 메인페이지로 리다이렉트
        return "redirect:/";
    }


    // 주소 설계
    // GET http://localhost:8080/user/update (화면 요청)
    @GetMapping("/user/update")
    public String updateForm(Model model, HttpSession session) {

        // 인증 검사
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        UserResponse.updateFormDto updateFormDto = userService.findUpdateForm(sessionUserDto.getId());
        model.addAttribute("user", updateFormDto);
        return "user/update-form";
    }

    // 주소 설계 - 회원 정보에 대한 수정 정보 요청
    // POST http://localhost:8080/user/update
    @PostMapping("/user/update")
    public String update(Model model, HttpSession session, UserRequest.UpdateDto updateDto) {

        // 1. 유효성 검사
        updateDto.Validate();

        // 2. 인증 검사 -> 로그인이 되어있는데 해야하나? -> 세션 유효기간이 만료되었을 때 필요
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        SessionUserDto updateSessionUserDto = userService.updateById(sessionUserDto.getId(), updateDto);

        // 3. 세션 동기화 처리 -> 수정된 비밀번호 정보를 업데이트하여 세션에 반영
        session.setAttribute(Define.SESSION_USER, updateSessionUserDto);

        // 4. 성공 후 메인페이지로 리다이렉트
        return "redirect:/user/detail";
    }

    // 로그아웃 경로
    // GET http://localhost:8080/logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        //세션 무효화 처리
        session.invalidate();
        // templates/ <- 콘텐츠 루트 경로
        return "redirect:/";
    }
}
