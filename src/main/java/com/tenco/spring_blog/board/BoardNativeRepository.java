package com.tenco.spring_blog.board;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

// 스프링으로 DAO 객체 설계중...

//final 필드에 대한 생성자를 .class 생성시 자동으로 생성한다.
@RequiredArgsConstructor // DI 처리
// -> 자동으로 힙 메모리에다가 new를 생성해줌
@Repository
//IoC(제어의 역전), @Repository: 스프링이 프레임워크가 데이터 접근 계층으로 인식,
// 데이터베이스 예외를 스프링 예외로 변환해주는 역할을 한다.
// 역할에 맞춰서 어노테이션을 사용하는 방법을 습득해야 한다.
public class BoardNativeRepository {

    // EntityManager JPA의 핵심 인터페이스
    // 데이터베이스와 모든 작업을 담당한다.
    // statement resultset의 역할 담당
    private final EntityManager em;

    @Transactional  // 싹 다 자동으로 해줌(커밋, 롤백, 클로즈 등....)
    public void save(String title, String content, String username) {
        Query query = em.createNativeQuery("insert into board_tb(title, content, username, created_at)" +
                "values(?, ?, ?, now())");

        // 값 바인딩
        query.setParameter(1, title);
        query.setParameter(2, content);
        query.setParameter(3, username);

        // select, i, u, d
        query.executeUpdate();
    }

    public List<Board> findAll() {
        String sql = """
                select * from board_tb order by id desc
                """;

        Query query = em.createNativeQuery(sql, Board.class);
        //while(rs.next) ...의 역할을 한다.
        // List<Board> boardList = query.getResultList();

        return query.getResultList();

        //단순 조회(select)라 트랜잭션 생략!
    }

    public Board findById(Long id) {
        String sql = """
                select * from board_tb where id = ?
                """;

        Query query = em.createNativeQuery(sql, Board.class);
        query.setParameter(1, id); // 값 바인딩
        try {
            // 형변환
            // 추후에 무슨 문제가 있을까? > 서버가 터져버림 > 예외 처리 필요!
            return (Board) query.getSingleResult();

        } catch (Exception e) {
            return null;
        }



    }

    @Transactional
    public void deleteById(Long id) {
        String sql = """
                delete from board_tb where id = ?
                """;
        Query query = em.createNativeQuery(sql);
        query.setParameter(1, id);
        query.executeUpdate();
    }

    @Transactional
    public boolean updateById(String title, String content, Long id) {

        String sql = """
                update board_tb set title = ?, content = ? where id = ?
                """;

        Query query = em.createNativeQuery(sql);
        query.setParameter(1, title);
        query.setParameter(2, content);
        query.setParameter(3, id);

        int rows = query.executeUpdate();
        if(rows > 0) {
            return true;
        } else {
            return false;
        }

    }

    // DI - 생성자를 생성함으로써 의존성 주입
//    public BoardNativeRepository(EntityManager em) {
//        this.em = em;
//    }
}
