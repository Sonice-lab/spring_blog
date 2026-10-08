package com.tenco.spring_blog._core.config;

import com.tenco.spring_blog._core.interceptor.IpBlockInterceptor;
import com.tenco.spring_blog._core.interceptor.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor // 의존성 주입
// @Component // 독립적으로 돌아가는 객체를 의미
@Configuration //  IoC 스프링 설정 클래스임을 표시(빈으로도 등록됨)
               // -> 안에 내부를 한 번 더 바라봤을 때
public class WebMvcConfig implements WebMvcConfigurer {

    // 로그인 인터셉트와의 관계는? -> 연관관계? 의존관계? -> 연관관계로 만들어보자.
    private final IpBlockInterceptor ipBlockInterceptor;
    private final LoginInterceptor loginInterceptor;


    // loginInterceptor를 시스템에 등록
    // 내가 정의한 인터셉트를 설정 클래스에 등록할 수 있다.
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 1. 차단한 IP 접근 경우 접속을 막기 위한 인터셉터
        registry.addInterceptor(ipBlockInterceptor)
                .addPathPatterns("/**");

        // 2. 로그인이 되지 않고는 접근할 수 없는 경우 로그인 요청 관련 인터셉터
        registry.addInterceptor(loginInterceptor)
            // 인터셉터가 동작할 URL 패턴을 지정
        .addPathPatterns("/user/**", "/board/**")
           // 인터셉터에서 제외할 URL 패턴을 지정할 수 있다.
        .excludePathPatterns("/board/list", "/board/{id:\\d+}");
          // \\d+는 정규 표현식으로 1개 이상의 숫자를 의미
         // 예시) /board/1, /board/123 (상세보기)는 로그인 없어도 접근 가능
        // /board/1/update  처럼 뒤에 경로가 더 붙는 경우 제외대상이 아니게 된다.
    }
}
