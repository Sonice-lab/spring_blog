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

@Slf4j // 무얼 위한 처리였더라?
@RequiredArgsConstructor //DI처리
@Controller // 이건 뭐더라?

public class BoardController {

    //DI 처리
    // 의존성 주입 처리를 왜 하더라????
    private  final BoardNativeRepository boardNativeRepository;

    //GET http://localhost:8080/   ,    http://localhost:8080/board/list

    @GetMapping({"/", "board/list"})
    public String list(Model model) {
        // 뼈대용 임시 데이터 주입
        model.addAttribute("boardList", List.of(
                Map.of("id", 1, "title", "첫번째 글"),
                Map.of("id", 2, "title", "두번째 글"),
                Map.of("id", 3, "title", "세번째 글")
        ));
        return "board/list";
    }

    // GET http://localhost:8080/board/save (화면 요청)

    @GetMapping("/board/save")
    public String saveForm() {
        return "board/save-form";
    }

    // [[코드 추가]]
    //POST http://localhost:8080/board/save (화면 요청)
    // 스프링 부트의 데이터 기본 파싱 전략 key=value
    // name 속성 기존으로 값을 추출할 수 있다.
    @PostMapping("/board/save")
    public String save (@RequestParam("username") String username,
                        @RequestParam("title") String title,
                        @RequestParam("content") String content) {

        // 폼의 name 속성과 매개변수명이 일치하면 자동으로 값이 바인딩됨
        // name="title" ---> String title로 자동 매핑
        log.info("username : {}", username);
        log.info("title : {}", title);
        log.info("content : {}", content);

        //DAO 객체에게 데이터를 전달 후 저장하는 일 위임
        boardNativeRepository.save(title, content, username);

        //redirect: 저장 후 메인 페이지로 이동
        //POST 요청 후 redirect로 PRG(Post-Redirect-Get) 패턴 구현
        return "redirect:/";
    }

    // GET http://localhost:8080/board/1/update (화면 요청)
    @GetMapping("/board/{id}/update")
    public String updateForm(@PathVariable Long id, Model model) {
        model.addAttribute("board", sampleBoard(id));
        return "board/update-form";
    }

    //todo
    //뼈대용 임시 게시글 한 개(데이터베이스 연결 시 삭제 예정)

    private  Map<String, Object> sampleBoard(Long id) {
        return Map.of("id", id,
                "title", id + "번째 글",
                "content", "임시 내용...",
                "username", "김민수");
    }
}
