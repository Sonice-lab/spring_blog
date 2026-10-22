package com.tenco.spring_blog.user;


import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception500;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog._core.util.MailUtil;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service //  Ioc
public class MailService {

    // 의존성을 설정했으며 추가로 application.yaml의 spring.mail 설정으로 스프링 부트가 만들어준 빈 객체이다.
    private final JavaMailSender javaMailSender;


    // 문법 활용) record: JDK 16 정식으로 사용할 수 있음
    // 이 아래의 한 줄은 클래스를 설계했다는 것을 의미한다.
    // 필드(멤버변수) + getter + equals + hashcode + toString을 가진 녀석을 자바가 대신 만들어준다.
    // 대신 setter가 없음, 불변 객체를 만들 때 사용, 상속 불가 -> 수정이 일어나지 않을 때 활용
    // 직렬화: 연속된 데이터 배열의 단위를 바이트 단위로 한 줄로 내려놓는다.
    private record EmailCode(String code, LocalDateTime expiresAt) implements Serializable {
        // 메서드 만들기
        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);

        }
    }

    // 1. 인증 번호 발송
    public void sendVerificationCode(String email, HttpSession session) {
        // 1-1. 6자리 인증번호 생성
        String code = MailUtil.generateCode();
        // 1-2. 메일 작성하는 부분(MimeMessage : HTML의 본문을 만들 수 있으며, 첨부 파일 등을 담을 수 있는 메일 형식이다.)
        MimeMessage message = javaMailSender.createMimeMessage();
        //외부 통신을 위한 try
        try {
            // false: 첨부 파일 없음, "UTF-8": 한글 깨짐 방지
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(email); //받는 사람 이메일 주소
            helper.setSubject("[스프링블로그] 회원가입 인증번호"); // 제목
            helper.setText("<h3>인증번호는 " + code + "입니다.</h3>" +
                    "<p>" + Define.CODE_EXPIRE_MINUTES + "분 안에 입력해주세요.</p>", true); // 본문 -> 실제로는 예쁘게 HTMl로 꾸며줍니다.:)

            // 1-3. 이메일 발송 -> 실패할 경우 에러 발생
            javaMailSender.send(message);

        } catch (Exception e) {
            throw new Exception500("메일 작성 중 오류가 발생했습니다.");
        }
        session.setAttribute(Define.EMAIL_CODE_PREFIX + email,
                new EmailCode(code, LocalDateTime.now().plusMinutes(Define.CODE_EXPIRE_MINUTES))); // 인증번호 유효 기간 - 5분 설정
        log.info("인증 번호 발송완료 - 이메일: {}", email); // 인증 번호 자체는 로그에 남기지 않는 것을 권장
    }

    // 2. 인증 번호 확인 : 일치하면 true 반환
    public boolean verifyCode(String email, String code, HttpSession session) {

        String key = Define.EMAIL_CODE_PREFIX + email;
        EmailCode savedEmailCode = (EmailCode) session.getAttribute(key);

        // emailCode_a@naver.com : 123456
        if (savedEmailCode == null) {
            throw new Exception400("인증번호를 먼저 요청해주세요.");
        }
        if (savedEmailCode.isExpired()) {
            session.removeAttribute(key);  //만료된 인증 번호는 세션에서 삭제 처리됨
            throw new Exception400("인증 번호 유효 기간이 만료되었습니다. 다시 요청해주세요.");
        }
        // [방어]잘못된 인증번호가 들어왔을 경우
        if (!savedEmailCode.code().equals(code)) {
            throw new Exception400("인증 번호가 일치하지 않습니다.");
        }
        // 성공했을 경우: 사용한 인증번호는 삭제 (한번만 사용할 수 있게)
        session.removeAttribute(key);

        // 인증을 마쳤을 경우 인증을 마친 이메일을 세션에 기록해야 함(회원가입 시 서버가 확인)
        // 인증된 임메일이 아닌 변조해서 던질 수 있음
        session.setAttribute(Define.VERIFIED_EMAIL, email); // 인증이 끝나면 이메일을 다시 체크해야하기에
        return true;
    }


}
