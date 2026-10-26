package com.tenco.spring_blog._core.error;

import com.tenco.spring_blog._core.util.Define;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// 모든 컨트롤러에서 발생하는 예외를 이 클래스에서 처리 (중앙 집중화)
@Slf4j
@ControllerAdvice //IoC 제어의 역전
public class GlobalExceptionHandler {

    // 특정 예외 타입이 발생했을 때 실행될 메서드로 지정
    @ExceptionHandler(Exception400.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String ex400(Exception400 e, HttpServletRequest request, Model model) {
        log.warn("=== 400 Bad Request 에러 발생 ===");
        log.warn("요청 URL: {}", request.getRequestURL());
        log.warn("에러메세지: {}", e.getMessage());
        log.warn("예외클래스: {}", e.getClass().getSimpleName());

        addSessionUserDto(request, model); //추가 - 헤더에 sessionUserDto 담기
        model.addAttribute("msg", e.getMessage());

        return "err/400";
    }

    // 특정 예외 타입이 발생했을 때 실행될 메서드로 지정
    @ExceptionHandler(Exception401.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String ex401(Exception401 e, HttpServletRequest request, RedirectAttributes rttr) {
        log.warn("=== 401 Unauthorized 에러 발생 ===");
        log.warn("요청 URL: {}", request.getRequestURL());
        log.warn("인증 오류: {}", e.getMessage());
        log.warn("예외클래스: {}", e.getClass().getSimpleName());

        rttr.addFlashAttribute("errorMessage", e.getMessage());

        // return "err/401";
        return "redirect:/login";
    }

    // 특정 예외 타입이 발생했을 때 실행될 메서드로 지정
    @ExceptionHandler(UnAuthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED) // HTTP 401 상태 코드 지정(alert 창 띄움)
    @ResponseBody // 화면을 뽑지 않고 데이터로 반환
    public String exUnauthorized(UnAuthorizedException e, HttpServletRequest request) {
        log.warn("=== 401(Data 반환) Unauthorized 에러 발생 ===");
        log.warn("요청 URL: {}", request.getRequestURL());
        log.warn("인증 오류: {}", e.getMessage());
        log.warn("예외클래스: {}", e.getClass().getSimpleName());

        String msg = e.getMessage().replace("'", "\\'"); //따옴표 깨짐 방지

        return """
                <script>
                    alert('%s');
                    location.href = '/login'
                </script>
                """.formatted(msg);
    }

    // 특정 예외 타입이 발생했을 때 실행될 메서드로 지정
//    @ExceptionHandler(Exception403.class)
//    public String ex403(Exception403 e, HttpServletRequest request, Model model) {
//        log.warn("=== 403 Forbidden 에러 발생");
//        log.warn("요청 URL: {}", request.getRequestURI());
//        log.warn("권한 오류: {}", e.getMessage());
//        log.warn("예외클래스: {}", e.getClass().getSimpleName());
//
//        model.addAttribute("msg", e.getMessage());
//
//        return "err/403";
//    }

    // alert 창이 뜰 수 있도록 수정
    @ExceptionHandler(Exception403.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN) //상태코드 403 지정(없으면 통신 성공으로 200으로 나감)
    public String ex403(Exception403 e, HttpServletRequest request, Model model) {
        log.warn("=== 403 Forbidden 에러 발생 ===");
        log.warn("요청 URL: {}", request.getRequestURL());
        log.warn("권한 오류: {}", e.getMessage());
        log.warn("예외클래스: {}", e.getClass().getSimpleName());

        String msg = e.getMessage().replace("'", "\\'"); //따옴표 깨짐 방지

        return """
                <script>
                    alert('%s');
                    history.back();
                </script>
                """.formatted(msg);
    }

    // 특정 예외 타입이 발생했을 때 실행될 메서드로 지정
    @ExceptionHandler(Exception404.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String ex404(Exception404 e, HttpServletRequest request, Model model) {
        log.warn("=== 404 Not Found 에러 발생 ===");
        log.info("요청 URL: {}", request.getRequestURL());
        log.info("에러메세지: {}", e.getMessage());
        log.info("예외클래스: {}", e.getClass().getSimpleName());

        addSessionUserDto(request, model); //추가 - 헤더에 sessionUserDto 담기
        model.addAttribute("msg", e.getMessage());

        return "err/404";
    }

    // 특정 예외 타입이 발생했을 때 실행될 메서드로 지정
    @ExceptionHandler(Exception500.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String ex500(Exception500 e, HttpServletRequest request, Model model) {
        log.error("=== 500 Internal Server Error 에러 발생 ===");
        log.error("요청 URL: {}", request.getRequestURL());
        log.error("서버 오류: {}", e.getMessage());
        log.error("스택 트레이스: ", e); // 전체 스택 트레이스 포함

        addSessionUserDto(request, model); //추가 - 헤더에 sessionUserDto 담기
        model.addAttribute("msg", "네트워크에 일시적인 장애");
        return "err/500";
    }

    // 기타 모든 RuntimeException처리 (최후의 보루)
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String ex500(RuntimeException e, HttpServletRequest request, Model model) {
        log.error("=== 예상치 못한 런타입 에러 발생 ===");
        log.error("요청 URL: {}", request.getRequestURL());
        log.error("에러 타입: {}", e.getClass().getSimpleName());
        log.error("에러메세지: {}", e.getMessage());
        log.error("스택 트레이스: ", e); // 전체 스택 트레이스 포함

        model.addAttribute("msg", "시스템 오류 발생. 관리자에게 문의해주세요.");
        return "err/500";
    }

    // 데이터베이스 제약 조건 위반(외래키, unique 중복 등)
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT) //409: 현재 데이터 상태와 충돌하는 것을 의미
    public String exDataIntegrityViolationException(DataIntegrityViolationException e, HttpServletRequest request, Model model) {
        log.error("=== 데이터베이스 제약 조건 위반 ===");
        log.error("요청 URL: {}", request.getRequestURL());
        // 상세 원인(SQL, 제약조건 이름)은 개발자용 로그에만 남김
       log.error("원인: {}", e.getMostSpecificCause().getMessage());

        model.addAttribute("msg", "시스템 오류 발생. 관리자에게 문의해주세요.");
        return "err/500";
    }

    // alert 창이 뜰 수 있도록 수정
    @ExceptionHandler(NotEnoughPointException.class)
    @ResponseBody
    public String exNotEnoughPointException(NotEnoughPointException e, HttpServletRequest request, Model model) {
        log.warn("=== 포인트 부족 에러 발생 ===");
        log.warn("요청 URL: {}", request.getRequestURL());

        String msg = e.getMessage().replace("'", "\\'"); //따옴표 깨짐 방지

        return """
                <script>
                    alert('%s');
                    location.href = '/user/detail';
                </script>
                """.formatted(msg);
    }

    // 오류 화면에서도 Header 의 로그인 메뉴가 보이도록 sessionUserDto Model에 추가
    private void addSessionUserDto(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        // 방어적코드
        if(session != null && session.getAttribute(Define.SESSION_USER) != null) {
            model.addAttribute(Define.SESSION_USER, session.getAttribute(Define.SESSION_USER));
        }
    }

}
