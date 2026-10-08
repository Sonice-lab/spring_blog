package com.tenco.spring_blog._core.interceptor;
import com.tenco.spring_blog._core.error.Exception403;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

// 특정 IP를 차단하는 인터셉터 구현 + 블랙리스트 표시
// 단 여러개가 가능해야 함 (조원들 IP 차단)
// 테스트 1) http://192.168.5.10:8080/
// 테스트 2) http://192.168.7.230:8080/

@Component
public class IpBlockInterceptor implements HandlerInterceptor {

    // 차단할 IP리스트(DB나 설정 파일에서 관리하는 것을 권장)
    private static final Set<String> BLOCKED_IPS = Set.of("192.168.5.10", "192.168.7.230");

    //컨트롤러 요청 전 가로채기
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 클라이언트 IP 추출
        String clientIp = getClientIp(request);
        System.out.println("현재 접속 IP: " + clientIp);

        // 차단된 IP인지 확인
        if(isBlockedIp(clientIp)) {
            // 차단된 IP라면 커스텀 예외 발생(컨트롤러로 넘어가지 않음)
            throw new Exception403("너는 이 사이트에 접근할 수 없어요.(진지)");
        }
        return true; // 접근 허용
    }

    // 5. 클라이언트의 실제 IP를 추출하는 로직
    private String getClientIp(HttpServletRequest request) {
        //프록시, 로드 밸런서를 거쳐온 경우 실제 IP가 담기는 헤더 확인
        String ip = request.getHeader("X-Forwarded-for");

        // 헤더에 값이 없으면 기본 방식(RemoteAddr)으로 추출
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            // 다중 프록시를 거친 경우 쉼표(,)로 구분되므로 첫번째 IP 만 추출
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    // 차단 목록 포함 여부 확인 메서드
    private boolean isBlockedIp(String ip) {
        //set 자료 구조의 contains()를 사용하여 해당 ip가 목록에 있는지 확인
        return BLOCKED_IPS.contains(ip);
    }

}
