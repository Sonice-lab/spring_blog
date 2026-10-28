package com.tenco.spring_blog.purchase;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PurchaseJpaRepository extends JpaRepository<Purchase, Long> {

    // 뭘 만들어야할까?
    // 1. 구매내역 조회 - 해당 게시글 pk 기준으로 검색

    @Query("select p from Purchase p where p.user.id = :userId and p.board.id = :boardId")
    Optional<Purchase> findByUserIdAndBoardId(@Param("userId") Long userId, @Param("boardId") Long boardId);

    // 2. 구매 여부 확인 - true, false 응답 필요시
    @Query("""
            select case when count(p) > 0 then true else false end
            from Purchase p
            where p.user.id = :userId and p.board.id = :boardId            
            """)
    boolean existsByUserIdAndBoardId(@Param("userId") Long userId, @Param("boardId") Long boardId);

    // 게시글을 구매한 건수 (게시글 삭제 가능 여부 확인)
    @Query("select count(p) from Purchase p where p.board.id = :boardId")
    long countByBoardId(@Param("boardId") Long boardId);


    // 3. 게시글에 달린 구매 내역 일괄 삭제 (게시글 삭제 전에 사용) --> 예시) 사용자 1번이 구매한 모든 구매내역을 삭제한다.
    @Modifying // select가 아니라면 반드시 설정(JPQL 쿼리를 작성했을 때)
    @Query("delete from Purchase p where p.board.id = :boardId")
    void deleteByBoardId(@Param("boardId") Long boardId);

    // 4. 로그인 사용자 구매 내역 조회
    // 가져와야할 것 : 구매내역 + 게시글 타이틀 + 게시글 작성자 이름을 한 번에 쿼리로 가져와야 한다.(최신 구매순 정렬)
    @Query("""
            select p from Purchase p 
            join fetch p.board b
            join fetch b.user
            where p.user.id = :userId
            order by p.id desc 
            """)
    List<Purchase> findAllByUserIdJoinBoard(@Param("userId") Long userId);

}
