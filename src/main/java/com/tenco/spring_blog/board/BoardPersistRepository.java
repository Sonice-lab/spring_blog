package com.tenco.spring_blog.board;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 영속성 컨텍스트를 활용한 Repository 클래스 만들기
 * Repository란? 저장소, 보관소, 창고를 의미한다.
 * 즉, 소프트웨어에서는 데이터를 저장하고 관리하는 곳을 추상화한 개념이다.
 */

@RequiredArgsConstructor //final 필드 초기화 처리
@Repository // IoC + 싱글톤
public class BoardPersistRepository {

    private final EntityManager em;

    @Transactional
    public void updateById(Long id, BoardRequest.UpdateDto reqDto) {
        // 1. 수정할 엔티티를 먼저 조회 후 영속 상태로 만듬
        Board boardEntity = em.find(Board.class, id);
        // 2. 방어적 코드 - 엔티티 존재 여부 확인
        if(boardEntity == null) {
            throw new IllegalArgumentException("수정할 게시글을 찾을 수 없습니다.");
        }
        // 업데이트를 할 시 1차 캐시를 저장하는 것
        // 엔티티 객체 상태 변경 중
//        boardEntity.setTitle(reqDto.getTitle());
//        boardEntity.setContent(reqDto.getContent());
        // 1차 캐시에 저장된 엔티티 객체의 내부 상태 값이 변경이 되고 트랜잭션이 종료가 되면
        // 더티 체킹(Dirty Checking)이 발생함
        // 1차 캐시의 상태와 DB의 상태가 다름 > 업데이트 쿼리를 자동으로 체크해줌 > 더티 체크의 개념
        boardEntity.update(reqDto);
    }

    // 게시글 삭제하기(영속성 콘텍스트를 활용한 안전한 삭제)
    @Transactional
    public void deleteByID(Long id) {
        // 1. 먼저 삭제할 엔티티를 영속 상태로 조회
        Board boardEntity = em.find(Board.class, id);

        // 2. 엔티티 존재 여부 확인(안전한 삭제)
        if(boardEntity == null) {
            throw new IllegalArgumentException("삭제할 게시글을 찾을 수 없습니다.");
        }

        // 3. 영속 상태의 엔티티를 삭제 상태로 변경
        em.remove(boardEntity);

        // 삭제 과정
        // board 엔티티가 영속 --> 삭제로 변경
        // 1차 캐시에서 해당 엔티티가 제거됨
        // 트랜잭션 커밋 시점에 DELETE SQL자동 실행됨

        //도전 과제: 삭제하는 JPQL 쿼리 만들어보기
        // delete from Board b where b.id = :id
        // delete from Board b where b.id = :id
//        Query query = em.createQuery("delete from Board b where b.id = :id");
//        query.setParameter("id", id);
//        query.executeUpdate();

//        return em.createQuery("delete from Board b where b.id = :id")
//                .setParameter("id", id)
//                .executeUpdate();
//

    }



    // 기능: 기본키로 게시글 단 건 조회(1차 캐쉬 활용)
    public Board findById(Long id) {
        Board board = em.find(Board.class, id);
        //find() 메서드의 특징:
        // 1. 기본키로만 조회 가능
        // 2. 1차 캐시에 먼저 찾기 시도
        // 3. 없으면 DB에서 조회 후 1차 캐시에 저장
        // 4. 영속 상태로 만든 후 반환하면 된다.

        return board;
    }

    //JPQL을 사용한 조회 방법
    public Board findByIdWithJPQL(Long id) {
        String jpql = """
                select b from Board b where b.id = :id
                """;

        try {
            return em.createQuery(jpql, Board.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
        // JPQL 의 단점
        // 1. 항상 1차 캐쉬를 우회하여 DB에 접근
        // 2. 코드가 복잡할 수 있음
        // 3. getSingleResult() 예외 처리 필요
    }

    // JPQL을 사용한 게시글 목록 조회
    public List<Board> findAll() {
        // JPQL: 엔티티 객체를 대상으로 하는 객체지향 쿼리
        // Board는 엔티티 클래스명, b 별칭으로 사용 가능
        // 테이블명(board_tb)이 아닌 엔티티명(Board)을 사용
        // 쿼리 설계
        String jpql = """
                select b from Board b order by b.createdAt desc
                """;
        // createQuery() 메서드: JQPL 쿼리 생성
        // 두번째 매개변수로 반환 타입을 지정(타입 안정성 확보)
        // getResultList(): List<Board> 반환
        return em.createQuery(jpql, Board.class).getResultList();
    }

    @Transactional  // 자바에서 웹표준 기술을 사용하기 위한 묶음을 의미 > 자카르타
    //게시글 저장 기능
    public Board save(Board board) {

        // 1. 매개변수로 받은 board는 이 시점에서 비영속상태(1차 캐시에 안 들어간 상태)하고 할 수 있다.
        //   - 아직 영속성 컨텍스트에 관리되지 않은 상태를 의미한다.
        //   - 데이터베이스와 연관 없는 순수 Java 객체인 상태
        em.persist(board);
        // 2. em.persist(board); 이후에 엔티티를 영속성 콘텍스트에 저장시킴
        //   - board 객체가 영속 상태로 변경됨
        //   - 영속성 콘텍스트가 엔티티를 관리하기 시작함
        //   - 아직 실제 INSERT 쿼리는 실행되지 않음 (쓰기 지연)

        // 3. 트랜잭션 커밋 시점에 실제 INSERT 쿼리가 실행됨
        //   - 이 때 영속성 콘텍스트의 변경 사항이 DB에 반영됨
        //   - board 객체의 id 필드에 자동 생성된 값이 할당됨.
        return board;

        // 4. 영속 상태의 객체를 반환
        //   - 자동으로 생성된 id 값일 포함한 객체가 반환됨.
    }

    // 엔티티의 영속 상태 4가지
    // 1. 비영속상태: 새로 생성된 객체, 영속성 콘텍스트와 무관
    // 2. 영속 상태: 영속성 콘텍스트의 관리되는 상태
    // 3. 준 영속 상태: Detach. 영속성 콘텍스트에서 분리된 상태
    // 4. 삭제 상태: 삭제 예정 상태(트랜잭션 커밋 시 DELETE 쿼리 실행)
    // 메서드 만들기
    private void entityLifecycleEx() {
        // 1. 비영속성 상태
        Board board = new Board("제목", "내용", "작성자");

        // 2. 영속 상태
        em.persist(board);

        // 3. 준영속 상태: 영속성 콘텍스트에서 분리된 상태
        em.detach(board);

        // 4. 삭제 상태 또는 삭제 예정 상태
        em.remove(board);
    }

}
