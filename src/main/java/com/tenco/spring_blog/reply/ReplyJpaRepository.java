package com.tenco.spring_blog.reply;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReplyJpaRepository extends JpaRepository<Reply, Long> {

    // 기본적인 CRUD 기능 안 만들어도 됨 -> 다 구현되어있기 때문
    /**
     * 자동으로 코드를 쓰지 않아도 되는 경우
     * hibernate가 직접 알아서 코드로 구현해준다.
     * 1. 등록 및 수정: save(Reply entity);
     *    - 엔티티를 DB에 저장해야한다.
     *    - ID가 없을 경우 자동으로 INSERT를 하며, ID가 있을 경우 자동으로 UPDATE를 실행
     *
     * 2. 단건 조회: findById(Long id)
     *    - ID로 엔티티를 조회하면 Optional<Reply> 타입을 반환
     *
     * 3. 전체 조회: findAll();
     *    - 테이블의 모든 데이터를 조회하며 List<Reply>로 반환
     *
     * 4. 삭제: deleteById(Long id);
     *    - 특정 ID를 가진 엔티티를 삭제
     *
     * 5. 데이터 갯수: count();
     *    - 전체 레코드의 개수를 반환
     *
     * 6. 존재 여부 확인: existById(Lond id);
     *    - 해당 ID를 가진 데이터가 있는지 확인하여 boolean 을 반환
     */

    // 1. 해당하는 특정 게시글의 댓글을 한 방에 가지고 오기
    // 게시글의 댓글 목록 조회(작성자와 함께 조회, 오래된 순부터 가지고 오기)
    @Query("select r from Reply r join fetch r.user where r.board.id = :boardId order by r.id asc")
    List<Reply> findAllByBoardIdJoinUser(@Param("boardId") Long boardId);

    // 주의!! @Query는 기본적으로 조회(select)로 실행된다. delete, update를 직접 쓸 때는
    // 반드시 @Modifying을 붙여야 한다.
    // 2. 게시글에 달린 댓글 일괄 삭제(게시글 삭제 전에 사용 예정)
    @Modifying
    @Query("delete from Reply r where r.board.id = :boardId")
    void deleteByBoardId(@Param("boardId") Long boardId);

}
