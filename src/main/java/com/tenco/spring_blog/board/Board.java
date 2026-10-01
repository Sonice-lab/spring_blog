package com.tenco.spring_blog.board;


import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
// 엔티티 클래스 만들기: 데이터 베이스 테이블 한 개를 자바 클래스로 그린 설계도
@Table(name="board_tb")
@Entity

public class Board {
    @Id // 이 필드가 기본키를 나타냄
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본키 값을 자동으로 생성(AUTO_INCREMENT)


    // 별도 어노테이션이 없을 경우 필드명이 컬럼명이 됨
    private Long id;
    private String content;
    private String title;
    private String username;

    private Timestamp createAt;

}
