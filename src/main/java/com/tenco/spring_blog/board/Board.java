package com.tenco.spring_blog.board;


import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.sql.Timestamp;

public class Board {
    @Id // 이 필드가 기본키를 나타냄
    @GeneratedValue // 기본키 값을 자동으로 생성(AUTO_INCREMENT)


    // 별도 어노테이션이 없을 경우 필드명이 컬럼명이 됨
    private Long id;
    private String content;
    private String title;
    private String username;

    private Timestamp createAt;

}
