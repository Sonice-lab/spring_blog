package com.tenco.spring_blog.board;


import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog._core.util.Define;
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

    // private final HttpSession session; -> 프로그램 구현시 무거워져서 권장하지 않음 -> 의존 관계로 활용하기

    @GetMapping({"/", "/board/list"})
    public String list(Model model) {
        List<Board> boardList = boardService.findAll();
        model.addAttribute("boardList", boardList);
        return "board/list";
    }

    // GET   http://localhost:8080/board/3  상세보기
    // excludePathPatters로 제외되어 로그인 접근 가능
    @GetMapping({"/board/{id}"})
    public String detail(@PathVariable(name = "id") Long id, Model model) {

        Board board = boardService.findById(id);
        model.addAttribute("board", board);
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
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        boardService.save(saveDto, sessionUser);
        return "redirect:/";
    }

    // todo - 인가 처리(추후) 서비스단으로 이동 예정
    // GET   http://localhost:8080/board/1/update  (수정 화면 요청)
    @GetMapping({"/board/{id}/update"})
    public String updateForm(@PathVariable Long id, Model model, HttpSession session) {
        Board board = boardService.findById(id);
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        if(!board.isOwner(sessionUser.getId())) {
            throw new Exception403("수정할 권한이 없습니다.");
        }
        model.addAttribute("board", board);
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
        //서비스 계층에세 업데이트로 넘김
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        boardService.updateByID(id, updateDto, sessionUser);

        // 성공했다면 - PRG 패턴 구현 -> 수정 완료 후 해당 게시글 상세보기로 리다이렉트 처리
        return "redirect:/board/" + id;

    }

    // 게시글 삭제
    @PostMapping("/board/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) throws Exception403 {

        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        boardService.deleteById(id, sessionUser);
        // 삭제 성공 후 메인 페이지 리다이렉트
        return "redirect:/";
    }
}
