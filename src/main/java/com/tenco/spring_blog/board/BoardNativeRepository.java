package com.tenco.spring_blog.board;


import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor // DI 처리
@Repository
// 제어의 역전, : 스프링이 데이터 접근 계층으로 인식
//데이터 베이스 예외를 스프링 예외로 변환해줌
public class BoardNativeRepository {
//EntityManage JPA의 핵심 인터페이스
//데이터 베이스와 모든 작업 담당

    private final EntityManager em;

    @Transactional
    public void save(String title, String content, String username) {
        Query query = em.createNativeQuery("insert into board_tb(title, content, username, created_at)" +
                "values(?, ?, ?, now())");

        query.setParameter(1, title);
        query.setParameter(2, content);
        query.setParameter(3, username);

        query.executeUpdate();
    }
}
