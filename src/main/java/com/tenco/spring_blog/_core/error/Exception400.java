package com.tenco.spring_blog._core.error;

// 400번대 - Bad Request 상황에서 사용할 사용자 정의 예외 클래스(커스텀)
// RuntimeException을 상속하여 언체크드 예외로 만듬
public class Exception400 extends RuntimeException {

    // 부모 클래스에 생성자가 있으면 어떤 키워드로 부를 수 있는가? -> super
    // 예외 메세지를 받을 수 있도록 String 파라미터 설계
    public Exception400(String msg) {
        super(msg); //부모 클래스의 메시지 설정
    }




}
