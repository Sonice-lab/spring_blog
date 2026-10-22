package com.tenco.spring_blog.user;


import com.tenco.spring_blog._core.error.Exception500;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog._core.util.MailUtil;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service //  Ioc
public class MailService {

    // 의존성을 설정했으며 추가로 application.yaml의 spring.mail 설정으로 스프링 부트가 만들어준 빈 객체이다.
    private final JavaMailSender javaMailSender;

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
            helper.setText("<h3>인증번호는 " + code + "입니다.</h3>", true); // 본문 -> 실제로는 예쁘게 HTMl로 꾸며줍니다.:)

            // 1-3. 이메일 발송 -> 실패할 경우 에러 발생
            javaMailSender.send(message);

        } catch (Exception e) {
            throw new Exception500("메일 작성 중 오류가 발생했습니다.");
        }
        session.setAttribute(Define.EMAIL_CODE_PREFIX + email, code);
        log.info("인증 번호 발송완료 - 이메일: {}", email); // 인증 번호 자체는 로그에 남기지 않는 것을 권장
    }

    // 2. 인증 번호 확인 : 일치하면 true 반환
    public boolean verifyCode(String email, String code, HttpSession session) {
        String savedCode = (String) session.getAttribute(Define.EMAIL_CODE_PREFIX + email);
        // emailCode_a@naver.com : 123456
        if (savedCode == null || savedCode.equals(code)) {
            return false;
        }

        // 사용한 인증번호는 삭제 (한번만 사용할 수 있게)
        session.removeAttribute(Define.EMAIL_CODE_PREFIX + email);

        // 인증을 마쳤을 경우 인증을 마친 이메일을 세션에 기록해야 함(회원가입 시 서버가 확인)
        // 인증된 임메일이 아닌 변조해서 던질 수 있음
        session.setAttribute(Define.VERIFIED_EMAIL, email); // 인증이 끝나면 이메일을 다시 체크해야하기에
        return true;
    }
}
