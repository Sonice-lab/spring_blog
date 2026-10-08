package com.tenco.spring_blog.controller;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog._core.util.Define;
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

    // POST http://localhost:8080/join
    @PostMapping("/join")
    //파싱 전략
    public String join(UserRequest.JoinDto joinDto, Model model) {

        //기능 코드 추가
        // 1. 인증 검사(이전에 로그인을 했는가?) -> 필요 없음 / 유효성 검사 -> 필요
        joinDto.Validate();

        // 회원가입을 하기 위한 로직
        // 2. 사용자명 중복 체크 확인

        User existingUser = userPersistRepository.findByUserName(joinDto.getUsername());

        if (existingUser != null) {
            throw new Exception400("이미 존재하는 사용자명입니다.");
        }

        //3. DTO 객체를 Entity로 변환
        User user = joinDto.toEntity();

        // 4. DB에 회원정보 저장 - 영속화됨
        User userEntity = userPersistRepository.save(user);
        // 유효성 검사 실패시
        //회원 가입 성공시 로그인 화면으로 이동
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

        //1. 입력데이터 검증
        loginDto.Validate();

        // 2. 사용자명과 비밀번호로 사용자 조회 요청
        User sessionUser = userPersistRepository.findByUsernameAndPassword(loginDto.getUsername(),
                loginDto.getPassword());

        // 3. 로그인 성공/실패 처리
        if (sessionUser == null) {
            // 로그인 실패: 일치하는 사용자 없음
            throw new Exception400
                    ("사용자명 또는 비밀번호가 올바르지 않습니다.");
        }

        // 머스태치가 세션 값을 기본으로 읽지 않는 설정이 되어있음
        // 머스태치 파일에서 세션 메모리에 접근할 수 있도록 설정을 추가해야 함 -> applicaion.yaml 공통에다가
        // 4. 로그인 성공: 세션에 사용자 정보를 저장

        // 보안상의 이유로 비밀번호 정보는 저장하고 싶지 않은 경우
        sessionUser.setPassword(null);
        session.setAttribute(Define.SESSION_USER, sessionUser);

        // 5. 성공시 메인페이지로 리다이렉트
        return "redirect:/";
    }


    // 주소 설계
    // GET http://localhost:8080/user/update
    @GetMapping("/user/update")
    public String updateForm(Model model, HttpSession session) {

        // 1. 인증 검사
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        User user = userPersistRepository.findById(sessionUser.getId());
        model.addAttribute("user", user);
        // templates/ <- 콘텐츠 루트 경로
        return "user/update-form";
    }

    // 주소 설계
    // POST http://localhost:8080/user/update
    @PostMapping("/user/update")
    public String update(Model model, HttpSession session, UserRequest.UpdateDto updateDto) {

        // 1. 인증 검사 -> 로그인이 되어있는데 해야하나? -> 세션 유효기간이 만료되었을 때 필요
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        // 2. 권한 검사 조회
        // 다른 사람의 정보는 처음부터 수정할 수 없음(대상이 실제로 있는지만 확인)
        User userEntity = userPersistRepository.findById(sessionUser.getId());
        if (userEntity == null) {
            throw new Exception404("사용자를 찾을 수 없습니다.");
        }
        // 3. 유효성 검사
        updateDto.Validate();

        // 4. 세션 동기화 -> 수정된 비밀번호 정보를 업데이트하여 세션에 반영
        User updateUser = userPersistRepository.updateById(sessionUser.getId(), updateDto);

        //동기화 처리
        updateUser.setPassword(null);
        session.setAttribute(Define.SESSION_USER, updateUser);

        // 5. 성공 후 메인페이지로 리다이렉트
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
