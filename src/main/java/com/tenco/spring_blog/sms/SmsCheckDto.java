package com.tenco.spring_blog.sms;

import com.tenco.spring_blog._core.error.Exception400;
import lombok.Data;

@Data
public class SmsCheckDto {
    private String phoneNumber; // 휴대폰 번호
    private String code; // 인증번호(확인할 경우 사용)

    public void validate() {
        if(phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new Exception400("휴대폰 번호를 입력해주세요.");
        }

        // 하이픈 제거 후 길이 검증
        String cleanNumber = phoneNumber.replaceAll("-", "");
        if(cleanNumber.length() < 10 || cleanNumber.length() > 11) {
            throw new Exception400("올바른 휴대폰 번호 형식이 아닙니다.");
        }
    }

    // 인증번호 확인 API에서 추가로 호출
    public void validateCode() {
        validate(); // 번호가 제대로 있는지 먼저 확인
        if(code == null || code.trim().isEmpty()) {
            throw new Exception400("인증번호를 입력해주세요.");
        }
        if(code.length() != 6) {
            throw new Exception400("인증번호는 6자리여야 합니다.");
        }
    }


}
