package com.tenco.spring_blog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller //IoC (제어의 역전) 싱글톤 패턴으로 관리됨
public class UserController {

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
    public String logout() {
        // templates/ <- 콘텐츠 루트 경로
        return "redirect:/";
    }


}
