package com.tenco.spring_blog.controller;

import com.tenco.spring_blog.user.User;
import com.tenco.spring_blog.user.UserPersistRepository;
import com.tenco.spring_blog.user.UserRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    // GET http://localhost:8080/login
    @GetMapping("/login")
    public String loginForm() {
        // templates/ <- 콘텐츠 루트 경로
        return "user/login-form";
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
