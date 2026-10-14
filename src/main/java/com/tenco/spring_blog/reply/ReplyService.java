package com.tenco.spring_blog.reply;

import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog.board.Board;
import com.tenco.spring_blog.board.BoardJpaRepository;
import com.tenco.spring_blog.user.SessionUserDto;
import com.tenco.spring_blog.user.User;
import com.tenco.spring_blog.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 서비스단: 레포지토리의 기능을 모아 하나의 서비스로 만들어서 트랜잭션 처리까지 함

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service //IoC
public class ReplyService {

    private final ReplyJpaRepository replyJpaRepository;
    private final BoardJpaRepository boardJpaRepository;
    private final UserJpaRepository userJpaRepository;

    //1. 게시글 댓글 목록 조회 기능(로그인 하지 않았을 경우 sessionUserId는 null이다.)
    public List<ReplyResponse.ListDto> findAllByBoardId(Long boardId, Long sessionUserId) {
        List<Reply> replyList = replyJpaRepository.findAllByBoardIdJoinUser(boardId);
        return replyList.stream()
                .map(reply -> new ReplyResponse.ListDto(reply, sessionUserId))
                .toList();

    }

    // 2. 댓글 작성 기능 -> Controller에서 호출한다.
    @Transactional
    public void save(ReplyRequest.SaveDto saveDto, SessionUserDto sessionUserDto) {
        // 비즈니스 로직 설계
        // 1. 게시글을 먼저 조회(실제로 게시글이 있는지 존재 여부 확인) - 보드 객체 생성
        // 잠시 자리를 비웠는데 댓글을 쓰기도 전에 게시글 작성자가 게시글을 삭제한 경우
        // save -> reply -> board, user 순서대로의 타입 필요
        Board board = boardJpaRepository.findById(saveDto.getBoardId())
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));

        // 2. 작성자 정보 조회 - 유저 객체 생성
        User user = userJpaRepository.findById(sessionUserDto.getId())
                        .orElseThrow(() -> new Exception404("작성자 정보를 확인할 수 없습니다."));

        // 3. 영속성 콘텍스트 처리 -> 저장 -> 한 번만 쓸 거라서 따로 객체를 생성하지 않고 바로 집어넣음.
        replyJpaRepository.save(saveDto.toEntity(board, user));

    }



    // 댓글 삭제 기능(내가 작성한 댓글이 맞는가?) - 댓글 1개 삭제
    // void 대신 Long을 활용할 수 있다.
    @Transactional
    public void deleteById(Long replyId, SessionUserDto sessionUserDto) {
        // 삭제 버튼을 눌렀을 때 댓글 pk만 받아서 삭제 처리
        // 1. 댓글 조회
        Reply reply = replyJpaRepository.findById(replyId)
                .orElseThrow(() -> new Exception404("댓글을 찾을 수 없습니다."));

        // 2. 권한 확인: 작성자 본인만 삭제 가능
        if(!reply.isOwner(sessionUserDto.getId())) {
            throw new Exception403("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        // 3. 삭제 처리
        replyJpaRepository.delete(reply);

    }

    //todo - 3. 해당 게시글이 삭제되었을 때 관련 댓글도 모두 삭제
}
