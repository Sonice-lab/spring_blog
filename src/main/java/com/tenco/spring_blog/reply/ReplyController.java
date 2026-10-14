package com.tenco.spring_blog.reply;


import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.SessionUserDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@Controller
public class ReplyController {

    private final ReplyService replyService;

    // 댓글 작성
    @PostMapping("/reply/save")
    public String save(ReplyRequest.SaveDto saveDto, HttpSession session) {
        // 1. 인증검사(로그인 여부) -> 로그인 인터셉트가 먼저 하고 있음 -> 안 적어도 됨
        // 2. 유효성 검사
        saveDto.validate();
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        // 3. 서비스 단으로 업무 위임 -> 저장 요청
        replyService.save(saveDto, sessionUserDto);
        // 4. 저장이 잘 끝나면 화면에 보여주어야 함 -> 리다이렉트 처리 -> PRG 설계
        return "redirect:/board/" + saveDto.getBoardId();
    }

    // 댓글 삭제
    // 주소설계: http://localhost:8080/reply/{id}/delete
    @PostMapping("/reply/{id}/delete")
    public String delete(@PathVariable Long id, @RequestParam(name = "boardId") Long boardId, HttpSession session) {
        // 1. 인증 검사 - 로그인 인터셉터가 하고 있음
        // 2. 유효성 검사 - 필요없음
        // 3. 서비스에 위임
        // 4. 리턴 - 해당 게시글
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        replyService.deleteById(id, sessionUserDto);
        // PRG 패턴 구현
        return "redirect:/board/" + boardId ;
    }

}
