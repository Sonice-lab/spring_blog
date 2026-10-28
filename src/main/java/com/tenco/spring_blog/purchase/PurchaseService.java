package com.tenco.spring_blog.purchase;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog.board.Board;
import com.tenco.spring_blog.board.BoardJpaRepository;
import com.tenco.spring_blog.user.User;
import com.tenco.spring_blog.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PurchaseService {

    private final BoardJpaRepository boardJpaRepository;
    private final PurchaseJpaRepository purchaseJpaRepository;
    private final UserJpaRepository userJpaRepository;

    // 서비스 단에서는 비즈니스 규칙을 만든다.
    // 유료게시글 구매 처리
    // 포인트 차감 _ 구매 내역 저장을 하나의 트랜잭션을 처리 -> 하나라도 실패하면 모두 롤백
    @Transactional
    public void purchase(Long boardId, Long userId) {
        // 1. 게시글 조회
        Board board = boardJpaRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));

        // 2. 실제 유료 게시글인지 확인
        if (!board.isPremium()) {
            throw new Exception400("유료 게시글이 아닙니다.");
        }

        // 3. 작성자는 자기 글을 구매할 수 없음(이미 열람 가능)
        if (board.isOwner(userId)) {
            throw new Exception400("자신이 작성한 게시글은 구매할 수 없습니다.");
        }

        // 4. 이미 구매했는지 확인 -> 중복 포인트 차감 방지
        if(purchaseJpaRepository.existsByUserIdAndBoardId(userId, boardId)) {
            throw new Exception400("이미 구매한 게시글입니다.");
        }

        // 5. 구매자 조회 -> 이유: 포인트 차감을 위함(또는 부족한지 확인 -> 롤백, 넉넉하다면 바로 차감)
        User user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new Exception400("사용자를 찾을 수 없습니다."));

        user.deductPoint(Board.PRIMIUN_PRICE);
        // 6. 구매 내역 데이터에 저장
        Purchase purchase = Purchase.builder()
                .user(user)
                .board(board)
                .price(Board.PRIMIUN_PRICE)
                .build();
        purchaseJpaRepository.save(purchase);


    }
    // 구매내역 조회 후 DTO 변환(트랜잭션 안)
    @Transactional(readOnly = true)
    public List<Purchase> purchaseList(Long userId) {

        // 1. 구매자 조회 -
        User user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new Exception400("사용자를 찾을 수 없습니다."));

        // 2.특정 아이디의 마이페이지에 구매내역 버튼을 누르면 해당 아이디가 구매한 내역이 모두 출력되도록 구현.
        return purchaseJpaRepository.findByUserIdWithBoardAndUser(userId);
    }

}
