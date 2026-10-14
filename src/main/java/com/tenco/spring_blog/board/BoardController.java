package com.tenco.spring_blog.board;

import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.reply.ReplyResponse;
import com.tenco.spring_blog.reply.ReplyService;
import com.tenco.spring_blog.user.SessionUserDto;
import com.tenco.spring_blog.user.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Slf4j
@RequiredArgsConstructor  // DI 처리
@Controller
public class BoardController {

    //DI 처리
    private final BoardService boardService;
    private final ReplyService replyService;

    // private final HttpSession session; -> 프로그램 구현시 무거워져서 권장하지 않음 -> 의존 관계로 활용하기
    @GetMapping({"/", "/board/list"})
    public String list(Model model) {
        List<BoardResponse.ListDto> boardList = boardService.findAll();
        model.addAttribute("boardList", boardList);
        return "board/list";
    }

    // GET   http://localhost:8080/board/3  상세보기
    // excludePathPatters로 제외되어 로그인 접근 가능
    @GetMapping({"/board/{id}"})
    public String detail(@PathVariable(name = "id") Long id, Model model, HttpSession session) {

        BoardResponse.DetailDto detailDto = boardService.findById(id);
        // 데이터베이스 조회가 필요한가?
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        // 로그인하지 않았으면 null 설정
        Long sessionUserId = (sessionUserDto == null) ? null : sessionUserDto.getId();

        // 로그인 완료했고, 작성자 본인일 때만 수정/삭제 버튼 표시
        boolean isOwner = sessionUserDto != null && detailDto.getUserId().equals(sessionUserDto.getId());

        // 댓글 목록 조회
        List<ReplyResponse.ListDto> replyList = replyService.findAllByBoardId(id, sessionUserId);

        model.addAttribute("board", detailDto);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("replyList", replyList);
        return "board/detail";
    }

    // GET   http://localhost:8080/board/save  (화면 요청)
    @GetMapping({"/board/save"})
    public String saveForm(HttpSession session) {
        return "board/save-form";
    }

    @PostMapping({"/board/save"})
    public String save(BoardRequest.SaveDto saveDto, HttpSession session) {
        saveDto.validate();
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        boardService.save(saveDto, sessionUserDto);
        return "redirect:/";
    }


    // GET   http://localhost:8080/board/1/update  (수정 화면 요청)
    @GetMapping({"/board/{id}/update"})
    public String updateForm(@PathVariable Long id, Model model, HttpSession session) {

        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        BoardResponse.UpdateFormDto updateFormDto = boardService.findByIdAndUpdateForm(id, sessionUserDto.getId());
        model.addAttribute("board", updateFormDto);
        return "board/update-form";
    }

    // POST   http://localhost:8080/board/1/update  (게시글 수정 기능 요청)
    // object를 통으로 받기
    @PostMapping({"/board/{id}/update"})
    public String update(@PathVariable Long id,
                         BoardRequest.UpdateDto updateDto,
                         HttpSession session) {

        // 유효성 검사
        updateDto.validate();
        //서비스 계층에서 업데이트로 넘김
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        boardService.updateByID(id, updateDto, sessionUserDto);

        // 성공했다면 - PRG 패턴 구현 -> 수정 완료 후 해당 게시글 상세보기로 리다이렉트 처리
        return "redirect:/board/" + id;

    }

    // 게시글 삭제
    @PostMapping("/board/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) throws Exception403 {

        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        boardService.deleteById(id, sessionUserDto);
        // 삭제 성공 후 메인 페이지 리다이렉트
        return "redirect:/";
    }
}
