package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.util.Define;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor //DI 처리
@Slf4j
@Controller //IoC (제어의 역전) 싱글톤 패턴으로 관리됨
public class UserController {

    private final UserService userService;

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
        User user = userService.login(loginDto);

        // 보안상의 이유로 비밀번호 정보는 저장하고 싶지 않은 경우 -> 있어야 함
        user.setPassword(null);
        session.setAttribute(Define.SESSION_USER, user);

        // 성공시 메인페이지로 리다이렉트
        return "redirect:/";
    }


    // 주소 설계
    // GET http://localhost:8080/user/update
    @GetMapping("/user/update")
    public String updateForm(Model model, HttpSession session) {

        // 인증 검사
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        User user = userService.findById(sessionUser.getId());
        model.addAttribute("user", user);
        return "user/update-form";
    }

    // 주소 설계
    // POST http://localhost:8080/user/update
    @PostMapping("/user/update")
    public String update(Model model, HttpSession session, UserRequest.UpdateDto updateDto) {

        // 1. 유효성 검사
        updateDto.Validate();

        // 2. 인증 검사 -> 로그인이 되어있는데 해야하나? -> 세션 유효기간이 만료되었을 때 필요
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        User updateUser = userService.updateById(sessionUser.getId(), updateDto);
        // 3. 세션 동기화 -> 수정된 비밀번호 정보를 업데이트하여 세션에 반영
        //동기화 처리
        updateUser.setPassword(null);
        session.setAttribute(Define.SESSION_USER, updateUser);
        // 4. 성공 후 메인페이지로 리다이렉트
        return "redirect:/";
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
