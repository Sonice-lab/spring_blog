package com.tenco.spring_blog.user;


import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.sms.SmsCheckDto;
import com.tenco.spring_blog.sms.SmsService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// 스크립트로 데이터를 반환함
@Slf4j
@RequiredArgsConstructor //DI 처리
@RestController
public class UserApiController {

    private final MailService mailService;
    private final UserService userService;
    private final SmsService smsService;
    private final UserJpaRepository userJpaRepository;

    // 1. 문자 전송 API
    @PostMapping("/api/sms/send")
    public ResponseEntity<?> sendSmsCode(@RequestBody Map<String, String> request, HttpSession session) {
        String phoneNumber = request.get("phoneNumber");

        // SmsService의 발송 메서드 호출
        smsService.sendVerificationCode(phoneNumber, session);

        return ResponseEntity.ok().body(Map.of("message", "인증번호가 발송되었습니다."));
    }

    // 2. 문자 확인 API (올려주신 코드 + URL 수정)
    @PostMapping("/api/sms/verify") // <-- 여기를 /api/sms/send 에서 /api/sms/verify 로 변경!!
    public ResponseEntity<?> verifySmsCode(@RequestBody SmsCheckDto smsCheckDto,
                                           HttpSession session) {
        smsCheckDto.validateCode();

        boolean verified = smsService.verifyCode(smsCheckDto.getPhoneNumber(), smsCheckDto.getCode(), session);
        if (!verified) {
            return ResponseEntity.badRequest().body(Map.of("message", "인증번호가 일치하지 않습니다."));
        }

        // 인증 성공 시 DB 업데이트 및 세션 정보 동기화
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        userService.updatePhoneNumber(sessionUserDto.getId(), smsCheckDto.getPhoneNumber());

        // 세션 정보 동기화 및 덮어쓰기 (명시적 저장)
        sessionUserDto.setPhoneNumber(smsCheckDto.getPhoneNumber());
        session.setAttribute(Define.SESSION_USER, sessionUserDto);

        return ResponseEntity.ok().body(Map.of("message", "휴대폰 인증이 완료되었습니다."));
    }

    // 데이터에 강제 저장 메서드
    @Transactional
    public void updatePhoneNumber(Long userId, String phoneNumber) {
        // 1. DB에서 기존 사용자 조회
        User user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new Exception400("사용자를 찾을 수 없습니다."));

        // 2. 휴대폰 번호 업데이트
        user.setPhoneNumber(phoneNumber);

        // 3. 강제 저장 (더티 체킹이 안 될 경우를 대비한 가장 확실한 쐐기 박기!)
        userJpaRepository.save(user);

        log.info("✅ DB 강제 저장 완료 - 유저 ID: {}, 번호: {}", userId, phoneNumber);
    }

    // POST /api/email/send http 1.1
    // 요청 바디: {"email" : "aaa@naver.com"}
    @PostMapping("/api/email/send")
    public ResponseEntity<?> sendCode(@RequestBody UserRequest.EmailCheckDto emailCheckDto, HttpSession session) {
        //  인증 검사 -> 회원가입하는 입장 -> 로그인 내역이 없기 때문에 할 수 없음
        // 유효성 검사 -> 유효한 회원가입 가능한 이메일인가
        // 이미 가입된 이메일이 있으면 400을 던져야 함
        emailCheckDto.validate();
        userService.checkEmailAvailable(emailCheckDto.getEmail()); // 사용 가능한 이메일임을 인증

        // SMTP를 사용해서 인증 번호 발송(구글 메일 서버) -> 사용자 이메일 수신함(인증번호)
        mailService.sendVerificationCode(emailCheckDto.getEmail(), session);

        return ResponseEntity.ok(Map.of("message", "인증번호가 발송되었습니다." + Define.CODE_EXPIRE_MINUTES + "분 안에 입력하세요.")); // 여기까지 왔다는 것은 인증이 되었다는 의미

    }

    //POST /api/email/verify
    //요청 바디 {"email": "a@naver.com", "code" : "123456"}
    @PostMapping("/api/email/verify")
    public ResponseEntity<?> verifyCode(@RequestBody UserRequest.EmailCheckDto emailCheckDto,
                                        HttpSession session) {
        emailCheckDto.validateCode();
        boolean verified = mailService.verifyCode(emailCheckDto.getEmail(), emailCheckDto.getCode(), session);
        if (!verified) {
            return ResponseEntity.badRequest().body(Map.of("message", "인증번호가 일치하지 않습니다."));
        }
        return ResponseEntity.ok().body(Map.of("message", "인증되었습니다."));
    }

    // 예외 처리
    // ================== 이 콘트롤러에서 발생한 예외는 JSON으로 별도 응답 처리 ================
    @ExceptionHandler(Exception400.class)
    public ResponseEntity<Map<String, String>> handleEx400(Exception400 e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    // 메일 발송 실패 등 그 밖의 오류 (500)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleEx500(RuntimeException e) {
        log.error("서버 내부 오류 발생: {}", e.getMessage()); // 디버깅을 위한 로그 오류 찍기
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "요청을 처리하는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.:)"));
    }
}
