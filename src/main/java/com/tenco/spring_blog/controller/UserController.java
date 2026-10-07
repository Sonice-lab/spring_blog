package com.tenco.spring_blog.controller;

import com.tenco.spring_blog.user.User;
import com.tenco.spring_blog.user.UserPersistRepository;
import com.tenco.spring_blog.user.UserRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

@RequiredArgsConstructor //DI 처리
@Slf4j
@Controller //IoC (제어의 역전) 싱글톤 패턴으로 관리됨
public class UserController {

    private final UserPersistRepository userPersistRepository;

    // 주소 설계
    // GET http://localhost:8080/join
    @GetMapping("/join")
    public String joinForm() {
        // templates/ <- 콘텐츠 루트 경로
        return "user/join-form";
    }

    // 주소 설계
    // POST http://localhost:8080/join
    // 파싱 전략
    public String join(UserRequest.JoinDto joinDto, Model model) {
        log.info("=== 회원가입 요청 ===");
        log.info("사용자명: {}", joinDto.getUsername());
        log.info("패스워드명: {}", joinDto.getPassword());
        log.info("이메일 주소: {}", joinDto.getEmail());

        try{
            // 기능 코드 추가
            // 1. 인증 검사(이전에 로그인을 했을 때 필요) -> 필요 없음/유효성 검사 -> 필요
            joinDto.Validate();

            // 회원가입을 하기 위한 로직
            // 2. 사용자명 중복 체크 확인
            User existingUser = userPersistRepository.findByUserName(joinDto.getUsername());

            if(existingUser != null) {
                throw new IllegalArgumentException("이미 존재하는 사용자명입니다.");
            }
            // 3. DTO 객체를 Entity로 변환
            User user = joinDto.toEntity();

            // 4. DB에 회원정보 저장 - 영속화됨
            User userEntity = userPersistRepository.save(user);
            //유효성 검사 실패시
            // 회원 가입 성공 시 로그인 화면으로 이동
            return "redirect:/login";
        } catch (Exception e) {
            log.error("회원가입 실패: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            return "user/join-form";
        }
    }


    // 주소 설계
    // GET http://localhost:8080/login
    // 미션 5 적용: 로그인 화면을 열 때 쿠키 읽기
    @GetMapping("/login")
    public String loginForm
        (@CookieValue(name = "rememberUsername", required = false) String rememberUsername, Model model) {
        // 쿠키가 존재하면 뷰로 값을 넘겨준다.
        if(rememberUsername != null) {
            model.addAttribute("rememberUsername", rememberUsername);
        }
        // templates/ <- 콘텐츠 루트 경로
        return "user/login-form";
    }

    // POST http://localhost:8080/login
    // 로그인 처리(예외적으로 POST 요청(보안상의 이유))
    @PostMapping("/login")
    public String login(UserRequest.LoginDto loginDto, HttpSession session, Model model, HttpServletResponse response) {
        log.info("--- 로그인 요청 ---");
        log.info("사용자명: {} ", loginDto.getUsername());

        try {
            //1. 입력데이터 검증
            loginDto.Validate();

            // 2. 사용자명과 비밀번호로 사용자 조회 요청
            User sessionUser = userPersistRepository.findByUsernameAndPassword(loginDto.getUsername(),
                    loginDto.getPassword());

            // 3. 로그인 성공/실패 처리
            if (sessionUser == null) {
                // 로그인 실패: 일치하는 사용자 없음
                throw new IllegalArgumentException("사용자명 또는 비밀번호가 올바르지 않습니다.");
            }

            // 머스태치가 세션 값을 기본으로 읽지 않는 설정이 되어있음
            // 머스태치 파일에서 세션 메모리에 접근할 수 있도록 설정을 추가해야 함 -> applicaion.yaml 공통에다가
            // 4. 로그인 성공: 세션에 사용자 정보를 저장
            session.setAttribute("sessionUser", sessionUser);
            log.info("로그인한 사용자: {} ", sessionUser.getUsername());

            // 미션 3 & 4 적용: 쿠키 생성 및 삭제 로직
            if(loginDto.isRememberId()) {
                // 미션 3: 체크하고 로그인 시 7일짜리 쿠키 저장
                Cookie cookie = new Cookie("rememberUsername", sessionUser.getUsername());
                // 7일 유지(초로 환산)
                cookie.setMaxAge(60 * 60 * 24 * 7);
                // 자바스크립트에서 읽을 수 없게 함(보안을 위해)
                cookie.setHttpOnly(true);
                // 애플리케이션 모든 경로에서 유효
                cookie.setPath("/");
                response.addCookie(cookie);

            } else {
                // 미션 4: 체크를 풀고 로그인 시 기존 쿠키 삭제
                Cookie cookie = new Cookie("rememberUsername", "");
                // 수명을 0으로 설정하여 삭제
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
            }

            // 5. 성공시 메인페이지로 리다이렉트
            return "redirect:/";

        } catch (Exception e) {
            //로그인 실패 시 에러메세지와 함께 로그인 폼으로 돌려보내기
            model.addAttribute("errorMessage", e.getMessage());
            return "user/login-form";
        }
    }



    // 주소 설계
    // GET http://localhost:8080/user/update
    @GetMapping("/user/update")
    public String updateForm(Model model) {
        // templates/ <- 콘텐츠 루트 경로

        //뼈대용 임시 데이터
        model.addAttribute("user",
                Map.of("username", "김민수", "email", "adc@naver.com"));
        return "user/update-form";
    }

    // 로그아웃 경로
    // GET http://localhost:8080/logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {

        log.info("=== 로그아웃 요청 ===");

        //세션 무효화 처리
        session.invalidate();
        log.info("로그아웃 완료");

        // templates/ <- 콘텐츠 루트 경로
        return "redirect:/";
    }


}
