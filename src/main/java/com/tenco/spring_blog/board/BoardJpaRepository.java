package com.tenco.spring_blog.board;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardJpaRepository extends JpaRepository<Board, Long> {
    // 기본적인 CRUD 기능 안 만들어도 됨 -> 다 구현되어있기 때문
    /**
     * 자동으로 코드를 쓰지 않아도 되는 경우
     * hibernate가 직접 알아서 코드로 구현해준다.
     * 1. 등록 및 수정: save(Board entity);
     *    - 엔티티를 DB에 저장해야한다.
     *    - ID가 없을 경우 자동으로 INSERT를 하며, ID가 있을 경우 자동으로 UPDATE를 실행
     * 2. 단건 조회: findById(Long id)
     *    - ID로 엔티티를 조회하면 Optional<Board> 타입을 반환
     * 3. 전체 조회: findAll();
     *    - 테이블의 모든 데이터를 조회하며 List<Board>로 반환
     * 4. 삭제: deleteById(Long id);
     *    - 특정 ID를 가진 엔티티를 삭제
     * 5. 데이터 갯수: count();
     *    - 전체 레코드의 개수를 반환
     * 6. 존재 여부 확인: existById(Lond id);
     *    - 해당 ID를 가진 데이터가 있는지 확인하여 boolean 을 반환
     */

    // 게시글 ID로 조회하면서 작성자 정보도 한 번에 가져오기
    // JOIN FETCH를 사용하여 N+1문제 해결
    // 추상 메서드
    // 네이티브 쿼리로도 활용 가능하나, 엄청나게 어려운 쿼리가 아닌 이상 잘 사용하지 않음
    @Query("SELECT b FROM Board b JOIN FETCH b.user u WHERE b.id = :id")
    Optional<Board> findByIdJoinUser(@Param("id") Long id);
    // N + 1 문제: 지연 로딩(LAZY Loading)에서 Board 조회 후 User를 따로 조회하면
    // 쿼리가 여러번 실행됨
    // JOIN FETCH는 이를 한 번의 쿼리로 해결함. -> 성능 문제 해결 (성능의 최적화)

    // 페이징 처리 전 버전
    @Query("SELECT b FROM Board b JOIN FETCH b.user u ORDER BY b.id DESC")
    List<Board> findAllJoinUser();

    // 게시글 페이징 조회(작성자와 함께 조인해서 조회) -> join fetch는 N+1 문제가 발생하지 않는다.
    // 오버로딩 가능 todo - 추후 이름 위의 메서드와 같이 변경
    // 정렬은 Pageable 에 담아서 전달하므로 쿼리에 order by를 쓰지 않는다.
    // 페이징 처리 후 버전 - 게시글 조회 + 사용자에 대한 정보 가져오기 + 페이징 처리 주문서
    // 데이터 타입 또한 바뀐다. -> Page


    @Query(value = "SELECT b FROM Board b JOIN FETCH b.user u",
            countQuery = "select count(b) from Board b")
    Page<Board> findAllJoinUser(Pageable pageable);

    // 성능 개선을 위한 업그레이드 -> countQuery 카운트만 가지고오기 때문에 성능이 훨씬 더 빠름
}
