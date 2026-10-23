package com.tenco.spring_blog._core.util;

public class Define {

    public static final String SESSION_USER = "sessionUserDto";

    // 이메일 인증
    public static final String EMAIL_CODE_PREFIX = "emailCode_"; // emailCode_a@naver.com
    public static final String VERIFIED_EMAIL = "verifiedEmail_"; // 인증을 마친 이메일 주소


    // 휴대폰(SMS) 인증
    public static final String SMS_CODE_PREFIX = "smsCode_"; // 뒤에 휴대폰 번호를 붙여 사용: smsCode_01012345678
    public static final String VERIFIED_PHONE = "verifiedPhone"; // 인증을 마친 휴대폰 번호

    public static final int CODE_EXPIRE_MINUTES = 5; // 인증번호 유효 시간 5분
}
