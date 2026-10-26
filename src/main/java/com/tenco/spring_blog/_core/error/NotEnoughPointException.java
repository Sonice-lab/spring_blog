package com.tenco.spring_blog._core.error;


public class NotEnoughPointException extends RuntimeException {
    public NotEnoughPointException(String msg) {
        super(msg);
    }
}
