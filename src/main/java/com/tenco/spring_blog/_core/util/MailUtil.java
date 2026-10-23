package com.tenco.spring_blog._core.util;

import java.security.SecureRandom;
import java.util.Random;


// Sms에서도 이 유틸은 재사용한다.
public class MailUtil {
    // SecureRandom: 예측하기 어려운 난수 생성지(보안을 위함)
    private static final SecureRandom RANDOM = new SecureRandom(); // 스레드라 상수로 선언해야한다.

    // 6자리 인증번호 생성하는 함수 만들기 (100000 ~ 999999)
    public static String generateCode() {
        int code = 100000 + RANDOM.nextInt(900000); //100000 + 0~899999
        return String.valueOf(code);
    }
}
