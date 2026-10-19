package com.tenco.spring_blog._core.interceptor;


import com.tenco.spring_blog._core.error.Exception401;
import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.Role;
import com.tenco.spring_blog.user.SessionUserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

import static com.tenco.spring_blog.user.Role.USER;

// 직접 구현해보기
// 관리자가 아니라면 해당 페이지 접근 차단!
@Component
public class AdminInterceptor implements HandlerInterceptor {

    // 컨트롤러 요청 전 가로채기
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. 세션에 로그인 정보 가져오기
        HttpSession session = request.getSession();
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);

        // 2. 비로그인 사용자 방어(LoginInterceptor를 먼저 거치더라도 안전을 위해 체크 권장)
        if(sessionUserDto == null) {
            throw new Exception401("로그인이 필요합니다.(관리자 전용)");
        }

        // 3. 관리자(ADMIN) 권한 체크
        if(!sessionUserDto.isAdmin()) {
            throw new Exception401("권한이 없습니다.(관리자 전용)");
        }
        return true;
    }


}
