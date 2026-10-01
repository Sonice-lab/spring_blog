package com.tenco.spring_blog.board;


import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
//엔티티 클래스 만들기: 데이터 베이스 테이블 한 개를 자바 클래스로 그린 설계도
@Table(name = "board_tb")
@Entity
public class Board {
    @Id // 이 필드가 기본키 임을 나타냄
    //기본키값을 자동으로 생성(IDENTITY -> DB의 기본 설정을 따른다.) AUTO_INCREMENT 기능 사용
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 별도 어노테이션이 없으면 자바의 필드명이 컬럼명이 된다.
    private String title;
    private String content;
    private String username;
    private Timestamp createdAt; //created_at 컬럼(스프링이 기본값인 스네이크 케이스로 자동 변환해줌)
}
