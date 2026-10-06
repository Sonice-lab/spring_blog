package com.tenco.spring_blog.user;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Getter
@NoArgsConstructor // 기본 생성자 필수(JPA 엔티티 생성자)
@Table(name="user_tb")
@Entity //자동 테이블 생성

public class User {





}
