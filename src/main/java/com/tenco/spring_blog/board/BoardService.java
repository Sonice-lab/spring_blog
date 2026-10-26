package com.tenco.spring_blog.board;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog.purchase.PurchaseJpaRepository;
import com.tenco.spring_blog.reply.ReplyJpaRepository;
import com.tenco.spring_blog.user.SessionUserDto;
import com.tenco.spring_blog.user.User;
import com.tenco.spring_blog.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Board 관련 비즈니스 로직을 처리하는 Service 계층
 * // 계층 분리 목적
 */
@Slf4j
@RequiredArgsConstructor //DI
@Service // IoC(싱글톤 패턴으로 관리됨)
//@Transactional()  // 동시성 제어 이슈 -> lock -> 안정성 보장 성능이 느려짐을 보완 -> 자카르타 불가
@Transactional(readOnly = true)
// 모든 메서드를 읽기 전용 트랜잭션으로 실행(findAll, findById 등 조회에 적합)
// 성능 최적화(변경 감지 비활성화), 데이터 수정 방지
//데이터베이스 락(lock)를 최소화해 동시성 성능 개선
public class BoardService {

    private final BoardJpaRepository boardJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final ReplyJpaRepository replyJpaRepository;
    private final PurchaseJpaRepository purchaseJpaRepository;

    /**
     * 게시글 저장
     *
     * @param saveDto     게시글 저장 정보
     * @param sessionUser 작성자 정보
     * @return 저장된 게시글 반환
     */
    @Transactional
    // 데이터 수정이 필요하므로 트랜잭션 읽기 전용 설정을 해제하고 쓰기 가능한 트랜잭션으로 실행
    public Board save(BoardRequest.SaveDto saveDto, SessionUserDto sessionUser) {
        log.info("게시글 저장 서비스 - 제목: {}, 작성자: {}",
                saveDto.getTitle(), sessionUser.getUsername());

        //Board board = saveDto.toEntity(sessionUser);
        User user = userJpaRepository.getReferenceById(sessionUser.getId());
        Board savedBoard = boardJpaRepository.save(saveDto.toEntity(user)); // pk, id, 생성시간이 포함되어있음

        log.info("게시글 저장 완료 - ID: {}, 제목: {}",
                savedBoard.getId(), savedBoard.getTitle());

        return savedBoard;
    }

    /**
     * 게시글 상세 조회
     *
     * @param id 게시글 PK
     * @return 게시글 정보 (작성자 정보 포함)
     */
    public BoardResponse.DetailDto findById(Long id, Long sessionUserId) {
        log.info("게시글 상세 조회 서비스 - ID : {} ", id);
        Board board = boardJpaRepository.findByIdJoinUser(id)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다"));
        log.info("게시글 상세 조회 완료 - 제목 : {}, 작성자 : {} ", board.getTitle(), board.getUser().getUsername());


        // 로그인 하지 않았으면 작성자도, 구매자도 아님
        boolean owner = sessionUserId != null && board.isOwner(sessionUserId);

        // 로그인을 했고, 구매 이력 테이블에 구매자로써 존재한다.(boardId, userId)
        boolean purchased = sessionUserId != null && purchaseJpaRepository.existsByUserIdAndBoardId(sessionUserId, id);

        return new BoardResponse.DetailDto(board, owner, purchased);

    }

    /**
     * 게시글 목록 조회 페이징 처리(메인 페이지용)
     *
     * @return 게시글 목록(작성자 정보 포함)
     */
    public BoardResponse.PageDto findAllPage(int page, int size, String keyword) {
        log.info("게시글 목록 조회 시작");

        // 1. 1부터 시작하는 번호를 0부터로 변환(0 이하가 들어와도 0으로 보장)
        // 클램프 알고리즘?
        int pageIndex = Math.max(0, page - 1);

        // 2. 페이지 크기는 -> 한 페이지에 몇 개를 보여줄 것인가? -> 1~50으로 제한 (size = 10000 과 같은 요청 방어)
        // 상황1. 사용자가 10000으로 던졌을 때 50이 나와야 한다. -> 천장 만들기
        // 상황2. 사용자가  -10000으로 던졌을 때 1이 나와야 한다. -> 바닥 만들기
        // 천장과 바닥을 만드는 것을 가두리 양식이라고 한다.
        //        아래의 코드를 줄인 것
//        int step1 = Math.min(50, size);
//        validSize = Math.max(1, step1);

        int validSize = Math.max(1, Math.min(50, size));

        // 최신글이 먼저 오도록 아이디 내림차순 정렬(주문서 생성)
        Pageable pageable = PageRequest.of(pageIndex, validSize, Sort.by(Sort.Direction.DESC, "id"));
        // isBlank: null이 아닌 상태에서 공백 제거 후 개수 확인
        Page<Board> boardPage;
        if (keyword == null || keyword.isBlank()) {
            boardPage = boardJpaRepository.findAllJoinUser(pageable);
        } else {
            boardPage = boardJpaRepository.findAllKeywordJoinUser(pageable, keyword);
        }

        return new BoardResponse.PageDto(boardPage);

    }

    // 사용자가 검색 키워드라는 key값을 만들것임
    // 빈 값으로 던지지 않았다면 서비스측에서 분리
    // 아무것도 안 넣었다면 ->


    /**
     * 게시글 수정 화면 요청 (인가 검사) - 권한 체크
     *
     * @param id
     * @param userId
     * @return
     */
    public BoardResponse.UpdateFormDto findByIdAndUpdateForm(Long id, Long userId) {
        Board board = boardJpaRepository.findByIdJoinUser(id)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));

        // 인가 검사
        if (!board.isOwner(userId)) {
            throw new Exception403("게시글 수정 권한이 없습니다.");
        }

        return new BoardResponse.UpdateFormDto(board);

    }


    /**
     * 게시글 수정(권한 체크)
     *
     * @param id          게시글 pk
     * @param updateDto   수정할 정보
     * @param sessionUser 요청자 정보
     * @return 수정된 게시글
     */
    // 게시글 수정
    @Transactional
    public Board updateByID(Long id, BoardRequest.UpdateDto updateDto, SessionUserDto sessionUser) {
        log.info("게시글 목록 수정 시작");

        // 수정할 때 조회먼저 실시
        // 1. 게시글 조회
        Board board = boardJpaRepository.findByIdJoinUser(id)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));

        // 2. 권한 체크
        if (!board.isOwner(sessionUser.getId())) {
            throw new Exception403("본인이 작성한 게시글만 수정할 수 있습니다.");
        }
        // 3. 더티체킹을 활용한 수정
        board.update(updateDto);

        // ★유효성 검사는 컨트롤러에서 진행

        log.info("게시글 목록 수정 완료");
        return board;
    }

    // 게시글 삭제
    @Transactional
    public void deleteById(Long id, SessionUserDto sessionUser) {
        log.info("게시글 삭제 시작");
        // 조회 시작
        Board board = boardJpaRepository.findByIdJoinUser(id)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));

        //권한 확인
        if (!board.isOwner(sessionUser.getId())) {
            log.warn("게시글 삭제 권한 없음 - 게시글 ID: {}, 작성자: {}. 요청자: {}",
                    id, board.getUser().getUsername(), sessionUser.getUsername());
            throw new Exception403("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }

        // 구매자가 있는 게시글은 삭제 불가(구매자의 구매 내역과 포인트 보호)
        if (purchaseJpaRepository.countByBoardId(id) > 0) {
            log.warn("구매 내역이 있어 게시글 삭제 불가 - 게시글 ID: {}", id);
            throw new Exception400("구매한 사용자가 있는 게시글은 삭제할 수 없습니다.");
        }


        // boardId로 댓글부터 먼저 삭제
        replyJpaRepository.deleteByBoardId(id);

        boardJpaRepository.deleteById(id);
        log.info("게시글 삭제 완료");
    }
}

// 방법 2 람다 표현식 (스트림 API 활용)
//List<BoardResponse.ListDto> dtoList = new ArrayList<>();
//         dtoList =  boardList.stream()
//                .map(board -> new BoardResponse.ListDto(board))
//                .toList();

// 방법 3
//        List<BoardResponse.ListDto> dtoList = new ArrayList<>();
//        for (Board board : boardList) {
//            dtoList.add(new BoardResponse.ListDto(board));
//        }


//        log.info("게시글 목록 조회 완료");
//        return boardList;
