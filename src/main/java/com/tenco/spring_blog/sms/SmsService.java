package com.tenco.spring_blog.sms;


import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import com.solapi.sdk.message.service.MessageHttpService;
import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception500;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog._core.util.MailUtil;
import com.tenco.spring_blog.user.MailService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

// [중요!] 솔라피 사이트 문서 참고!) 생성자를 직접 만들어서 객체를 생성해서 사용해야 함

@Slf4j // 객체 생성없이 로그를 찍기 위함
@RequiredArgsConstructor //DI 주입
@Service // IoC
public class SmsService {

    @Value("${solapi.api-key}")
    private String apiKey;

    @Value("${solapi.api-secret}")
    private String apiSecret;

    @Value("${solapi.sender}")
    private String sender;

    private DefaultMessageService messageService;

    // 빈 생성 후 최신 SolapiClient 방식으로 객체 초기화
    @PostConstruct
    public void init() {
        this.messageService = SolapiClient.INSTANCE.createInstance(apiKey, apiSecret);
    }

    // 1.인증번호 발송
    public void sendVerificationCode(String phoneNumber, HttpSession session) {
        // 1-1. 6자리 인증번호 생성
        String code = MailUtil.generateCode();
        log.info("======[테스트용] 발급된 인증번호: {}=======", code);
        // 1-2. 문자 작성
        Message message = new Message();
        String cleanPhoneNumber = phoneNumber.replaceAll("-", "");
        String cleanFromNumber = sender.replaceAll("-", "");

        message.setTo(cleanPhoneNumber);
        message.setFrom(cleanFromNumber);
        message.setText("[스프링블로그] 휴대폰 인증번호는 [" + code + "] 입니다." +
                Define.CODE_EXPIRE_MINUTES + "분 안에 입력해주세요.");

        try {
            // 1-3. 발송
            this.messageService.send(message);
        } catch (SolapiMessageNotReceivedException e) {
            // 발송 실패 내역 상세 로깅
            log.error("SMS 발송 실패 목록: {}", e.getFailedMessageList());
            log.error("SMS 발송 실패 원인: {}", e.getMessage());
            throw new Exception500("SMS 발송 중 오류가 발생했습니다.(일부 실패)");
        } catch (Exception e) {
            log.error("SMS 서버 통신 실패: {}", e.getMessage());
            throw new Exception500("SMS 발송 서버와 통신 중 오류가 발생했습니다.");
        }


        // 인증번호 세션 저장
        session.setAttribute(Define.SMS_CODE_PREFIX + phoneNumber,
                new SmsCode(code, LocalDateTime.now().plusMinutes(Define.CODE_EXPIRE_MINUTES)));
        log.info("인증번호 발송 완료 - 휴대폰: {}", phoneNumber);
    }

    // 2. 인증번호 확인 로직 (유효시간 검사 추가)
    public boolean verifyCode(String phoneNumber, String code, HttpSession session) {
        // 1. String이 아닌 SmsCode 객체로 꺼내옵니다.
        SmsCode smsCode = (SmsCode) session.getAttribute(Define.SMS_CODE_PREFIX + phoneNumber);

        // 2. 세션에 값이 없으면(만료되었거나 발송한 적 없으면) 실패
        if (smsCode == null) {
            return false;
        }

        // 3. 유효시간 검사: 현재 시간이 만료 시간을 지났는지 확인
        if (LocalDateTime.now().isAfter(smsCode.getExpiryTime())) {
            session.removeAttribute(Define.SMS_CODE_PREFIX + phoneNumber); // 쓰레기 데이터 삭제
            throw new Exception400("인증번호 유효시간(" + Define.CODE_EXPIRE_MINUTES + "분)이 초과되었습니다.");
        }

        // 4. 인증번호 일치 여부 확인
        if (smsCode.getCode().equals(code)) {
            session.removeAttribute(Define.SMS_CODE_PREFIX + phoneNumber);

            // 회원님이 작성하셨던 인증 완료 마킹 로직 유지!
            session.setAttribute(Define.VERIFIED_PHONE, phoneNumber);
            return true;
        }

        return false;
    }

    // SmsService.java 내부 하단
    public static class SmsCode {
        private String code;
        private LocalDateTime expiryTime;

        public SmsCode(String code, LocalDateTime expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }

        public String getCode() { return code; }
        public LocalDateTime getExpiryTime() { return expiryTime; }
    }

}
