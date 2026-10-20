package com.tenco.spring_blog._core.interceptor;


import com.tenco.spring_blog._core.error.Exception401;
import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.error.UnAuthorizedException;
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
// 관리자 전용 페이지 보호
// Admin컨트롤러에 들어오기 전에 처리
@Component
public class AdminInterceptor implements HandlerInterceptor {

    // 컨트롤러 요청 전 가로채기
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 로그인 여부 확인
        // 확인만 해야하므로 새로운 세션을 생성해서는 안됨 -> false 설정 필수
        HttpSession session = request.getSession(false);
        // nullpointerException 방어적 코드
        SessionUserDto sessionUserDto = (session == null) ? null : (SessionUserDto) session.getAttribute(Define.SESSION_USER);

        // 1. 로그인 확인은 사실 우리가 만들었던 LoginInterceptor가 먼저 처리
        // 그러나, 등록 순서가 바뀔 경우 대참사가 일어난다.
        if (sessionUserDto == null) {
            throw new UnAuthorizedException("로그인 먼저 진행해주세요.(관리자 전용)");
        }

        // 2. 관리자가 아니면 접근 거부
        if (!sessionUserDto.isAdmin()) {
            throw new Exception403("관리자만 접근할 수 있는 페이지입니다.");
        }

        return true;
    }


}
