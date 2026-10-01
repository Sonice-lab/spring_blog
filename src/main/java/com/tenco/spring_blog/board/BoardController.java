package com.tenco.spring_blog.board;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor  // DI 처리
@Controller
public class BoardController {

    //DI 처리
    private final BoardNativeRepository boardNativeRepository;

    // GET http://localhost:8080/    ,  http://localhost:8080/board/list
    @GetMapping({"/", "/board/list"})
    public String list(Model model) {

        // 역할과 책임에 따라서 데이터에 접근 > boardNativeRepository
        List<Board> boardList = boardNativeRepository.findAll();
        model.addAttribute("boardList", boardList);


        return "board/list";

    }

    // GET   http://localhost:8080/board/3
    @GetMapping({"/board/{id}"})
    public String detail(@PathVariable(name = "id") Long id, Model model) {

        // 조회 기능 만들기

        Board board = boardNativeRepository.findById(id);
        if(board == null) {
            return "redirect:/";
        }

        model.addAttribute("board", board);

        return "board/detail";
    }

    // GET   http://localhost:8080/board/save  (화면 요청)
    @GetMapping({"/board/save"})
    public String saveForm() {
        // 뼈대용 임시 데이터
        return "board/save-form";
    }

    // 코드 추가
    // POST  http://localhost:8080/board/save  (최초 글쓰기 후 저장 화면 요청)
    // 스프링 부트의 데이터 기본 파싱 전략 key=value
    // name 속성 기준으로 값을 추출할 수 있다.
    @PostMapping({"/board/save"})
    //데이터 바인딩 처리
    public String save(@RequestParam("username") String username,
                       @RequestParam("title") String title,
                       @RequestParam("content") String content) {

        //폼의 name 속성과 매개변수 명이 일치하면 자동으로 값이 바인딩 됨.
        //name = "title" --> String title로 자동 매핑

        log.info("username: {}", username);
        log.info("title: {}", title);
        log.info("content: {}", content);

        // DAO 객체에게 데이터를 전달 후 저장하는 일을 위임한다.
        boardNativeRepository.save(title, content, username);
        // redirect:/ : 저장 후 메인페이지로 이동
        // POST 요청 후 redirect 처리 > PRG(Post-Redirect-Get) 패턴 구현
        return "redirect:/";
    }

    // GET   http://localhost:8080/board/1/update  (수정 화면 요청)
    @GetMapping({"/board/{id}/update"})
    public String updateForm(@PathVariable Long id, Model model) {

       // 수정하기 화면 요청(먼저 조회부터)
        Board board = boardNativeRepository.findById(id);
        model.addAttribute("board", board);

        return "board/update-form";
    }

    // POST   http://localhost:8080/board/1/update  (게시글 수정 기능 요청)
    @PostMapping({"/board/{id}/update"})
    public String update(@PathVariable Long id,
                         // 폼태그에서 기본 파싱 전략
                         @RequestParam(name = "title") String title,
                         @RequestParam(name = "content") String content) {

        boardNativeRepository.updateById(title, content, id);
        //PRG 패턴 구현
        return "redirect:/board/" + id; //리다이렉트 수정된 게시글 상세보기 화면 이동
    }

    // 게시글 삭제
    // 주소 설계
    @PostMapping("/board/{id}/delete")
    public String delete(@PathVariable Long id) {

        boardNativeRepository.deleteById(id);

        //PRG 패턴 활용
        return "redirect:/";

    }


}
