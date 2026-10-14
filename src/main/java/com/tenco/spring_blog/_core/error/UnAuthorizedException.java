package com.tenco.spring_blog._core.error;

// 로그인 페이지로 이동할 수 있도록
public class UnAuthorizedException extends RuntimeException {
    public UnAuthorizedException(String msg) {
        super(msg);
    }
}
