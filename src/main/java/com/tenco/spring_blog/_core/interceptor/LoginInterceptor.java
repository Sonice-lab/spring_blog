package com.tenco.spring_blog._core.interceptor;

import com.tenco.spring_blog._core.error.Exception401;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component // IoC(싱글톤으로 관리)
public class LoginInterceptor implements HandlerInterceptor {

    // 컨트롤러단에 들어가기 전에 동작시킬 수 있는 핸들러
    // (단, 설정 클래스에 등록되어있고, 지정한 URL 패턴에 해당할 때만 동작)
    // 리턴 타입이 TRUE일 경우 컨트롤러 안으로 들여보낸다.
    // 리턴 타입이 FALSE일 경우 컨틀롤러 안으로 들어갈 수 없다.
    // 로그인이 되어야지만 접근할 수 있다.
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // getSession(false): 세션이 없으면 새로 만들지 않고 null 반환
        // request.getSession(): 로그인하지 않은 사용자에게도  빈 세션이 생성된다.
        HttpSession session = request.getSession(false);
        // 코드를 줄여보자.
        User sessionUser = (session == null) ? null : (User) session.getAttribute(Define.SESSION_USER);
        if(sessionUser == null) {
            throw new Exception401("로그인 먼저 진행해주세요.");
            // 로그인 페이지로 바로 이동하게 하고싶음

        }
        return true;
    }

    // 컨트롤러 실행 후 뷰가 랜더링되기 바로 직전에 호출시킬 수 있다.
    // 주의: ... 컨트롤러에서 예외가 발생하면 호출되지 않는다.
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }


    // 요청 처리가 완료된 후, 즉, 뷰가 완전히 랜더링된 후에 호출시킬 수 있음.
    // 예외가 발생해도 호출되며, 발생한 예외는 ex 매개변수로 전달된다.
    // 단, preHandle이 true를 반환한 경우에만 호출된다.
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
