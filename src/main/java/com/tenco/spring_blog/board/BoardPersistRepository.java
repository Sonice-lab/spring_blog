package com.tenco.spring_blog.board;


import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 영속성 컨텍스트를 활용한 Repository 클래스 만들기
 * Repository란? 저장소, 보관소, 창고를 의미한다.
 * 즉, 소프트웨어에서는 데이터를 저장하고 관리하는 곳을 추상화한 개념이다.
 */

// =================================================================
// [1단계] 클래스 기본 설정 및 의존성 주입
// =================================================================
// 1. 이 클래스가 Spring의 저장소(Repository) 역할을 한다는 것을 스프링 컨테이너에 알려주기 (빈 등록)
@Repository
// 2. final이 붙은 필드를 모아 자동으로 생성자를 만들어주는 롬복(Lombok) 설정하기
@RequiredArgsConstructor
public class BoardPersistRepository {
// 3. JPA의 핵심인 영속성 컨텍스트를 관리하는 관리자(EntityManager)를 final 필드로 선언하기

    private final EntityManager em;

    // =================================================================
// [2단계] 데이터 저장 (Create)
// =================================================================
// 1. 메서드 목적: 비영속 상태의 새 게시글 객체를 받아 DB에 저장한다.
// 2. DB에 변경(쓰기)이 일어나므로 작업 단위(트랜잭션)를 묶어주기
    @Transactional
    public Board save(Board board) {
        // 3. 전달받은 순수 자바 객체(Entity)를 EntityManager를 통해 '영속 상태'로 만들기
        //  - 아직 실제 INSERT 쿼리는 실행되지 않음(쓰기 지연)
        em.persist(board);
        // 4. (트랜잭션이 종료될 때 INSERT 쿼리가 날아감을 기억하며) 영속 상태가 된 객체 반환하기
        //  - board 객체의 id 필드에 DB가 자동 생성한 PK 값이 할당된 채로 반환됨
        return board;
    }

    // =================================================================
// [3단계] 데이터 단건 조회 (Read - 1차 캐시 활용)
// =================================================================
// 1. 메서드 목적: PK(id)를 이용해 게시글 하나를 찾는다. (읽기 전용이므로 트랜잭션 불필요)
    public Board findById(Long id) {
        // 2. EntityManager가 제공하는 기본 검색 기능을 사용해 해당 id의 엔티티 찾기
        // 3. 머릿속으로 흐름 그리기: '1차 캐시를 먼저 뒤지고 -> 없으면 DB에서 찾아서 -> 1차 캐시에 올린 후 -> 반환한다'
        Board board = em.find(Board.class, id);

        // 4. 찾아온 엔티티 반환하기
        return board;
    }

    // =================================================================
// [3-1단계] 데이터 단건 조회 (Read - JPQL 활용)
// =================================================================
// 1. 메서드 목적: JPQL을 사용해 PK(id)로 게시글 하나를 찾는다.
    public Board findByIdWithJPQL(Long id) {
        // 2. 테이블이 아닌 '객체'를 대상으로 하는 쿼리문(String) 작성하기
        String jpql = """
                      select b from Board b where b.id = :id
                      """;

        // 3. 예외 처리를 위한 try-catch 블록 열기
        try {
            // 4. EntityManager를 통해 JPQL 쿼리를 만들고 -> 파라미터(id)를 바인딩하고 -> 단일 결과(SingleResult) 가져오기
            return em.createQuery(jpql, Board.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (Exception e) {
            // 5. 결과가 없어서 에러가 발생하면 null을 반환하도록 catch 블록 처리하기
            return null;
        }
    }


// =================================================================
// [4단계] 데이터 다건 조회 (Read - 목록)
// =================================================================
// 1. 메서드 목적: 여러 개의 게시글 목록(List)을 최신순으로 가져온다.
    public List<Board> findAll() {
// 2. 전체 엔티티를 조회하고 정렬하는 JPQL 쿼리문(String) 작성하기
        String jpql = """
                select b from Board b order by b.createdAt desc
                """;
// 3. EntityManager를 통해 쿼리를 생성할 때, 반환될 데이터의 '타입'도 함께 지정해 주기
// 4. 쿼리를 실행하고 결과를 리스트(List) 형태로 뽑아서 반환하기
        return em.createQuery(jpql, Board.class).getResultList();
    }

// =================================================================
// [5단계] 데이터 수정 (Update - 더티 체킹)
// =================================================================
// 1. 메서드 목적: 특정 id의 게시글을 찾아 내용을 수정한다.
// 2. 데이터 변경(쓰기)이 일어나므로 작업 단위(트랜잭션) 묶어주기
    @Transactional
    public void updateById(Long id, BoardRequest.UpdateDto reqDto) {
// 3. [조회]: 수정할 엔티티를 id로 검색하여 '영속 상태'로 만들기 (1차 캐시에 올리기)
        Board boardEntity = em.find(Board.class, id);
// 4. [검증]: 방어적 코드 작성 -> 만약 조회한 엔티티가 없다면(null) 예외(Exception) 던지기
        if(boardEntity == null) {
            throw new IllegalArgumentException("수정할 게시글을 찾을 수 없습니다.");
        }
// 5. [수정]: 영속 상태인 엔티티의 데이터를 새로운 데이터(DTO)로 덮어씌우기
// 6. 머릿속으로 흐름 그리기: '별도로 save나 update 메서드를 부르지 않아도,
// 트랜잭션이 끝날 때 1차 캐시와 데이터가 다르면(더티 체킹) 자동으로 UPDATE 쿼리가 날아간다'
        boardEntity.update(reqDto);
    }

// =================================================================
// [6단계] 데이터 삭제 (Delete)
// =================================================================
// 1. 메서드 목적: 특정 id의 게시글을 찾아 삭제한다.
// 2. 데이터 변경(삭제)이 일어나므로 작업 단위(트랜잭션) 묶어주기
    @Transactional
    public void deleteById(Long id) {
// 3. [조회]: 삭제할 엔티티를 id로 검색하여 '영속 상태'로 만들기
        Board boardEntity = em.find(Board.class, id);
// 4. [검증]: 방어적 코드 작성 -> 만약 조회한 엔티티가 없다면(null) 예외(Exception) 던지기
        if(boardEntity == null) {
            throw new IllegalArgumentException("삭제할 게시글을 찾을 수 없습니다.");
        }
// 5. [삭제]: EntityManager를 이용해 영속 상태의 엔티티를 '삭제 상태'로 변경하기
// 6. 머릿속으로 흐름 그리기: '트랜잭션이 커밋되는 시점에 실제로 DELETE 쿼리가 날아간다'
        em.remove(boardEntity);

        // (도전 과제 참고) 영속성 컨텍스트를 거치지 않고 직접 쿼리를 날릴 경우의 예시
        // em.createQuery("delete from Board b where b.id = :id")
        //   .setParameter("id", id)
        //   .executeUpdate();
    }

// =================================================================
// [부록] 엔티티의 생명주기 연습 (개념 복습용)
// =================================================================
    private void entityLifecycleEx() {
// 1. 비영속 상태: 순수한 새 객체 만들기 (new)
        Board board = new Board("제목", "내용", "작성자");
// 2. 영속 상태: EntityManager를 통해 객체 관리 시작하기
        em.persist(board);
// 3. 준영속 상태: 영속성 컨텍스트에서 해당 객체 분리해보기(detach)
        em.detach(board);
// 4. 삭제 상태: 객체를 삭제 상태로 전환해보기(remove)
        em.remove(board);
    }
}
