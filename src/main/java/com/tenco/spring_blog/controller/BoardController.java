package com.tenco.spring_blog.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@Controller
public class BoardController {

    // GET http://localhost:8080/    ,  http://localhost:8080/board/list
    @GetMapping({"/", "/board/list"})
    public String list(Model model) {

        // 뼈대용 임시 데이터
        model.addAttribute("boardList", List.of(
                Map.of("id", 1, "title", "첫번째 글"),
                Map.of("id", 2, "title", "두번째 글"),
                Map.of("id", 3, "title", "세번째 글")
        ));

        return "board/list";

    }

    // GET   http://localhost:8080/board/3
    @GetMapping({"/board/{id}"})
    public String detail(@PathVariable(name = "id") Long id, Model model) {
        model.addAttribute("board", sampleBoard(id));
        // 뼈대용 임시 데이터
        return "board/detail";
    }

    // GET   http://localhost:8080/board/save  (화면 요청)
    @GetMapping({"/board/save"})
    public String saveForm() {
        // 뼈대용 임시 데이터
        return "board/save-form";
    }

    // GET   http://localhost:8080/board/1/update  (수정 화면 요청)
    @GetMapping({"/board/{id}/update"})
    public String updateForm(@PathVariable Long id, Model model) {
        model.addAttribute("board", sampleBoard(id));
        // 뼈대용 임시 데이터
        return "board/update-form";
    }

    //TODO
    //뼈대용 임시 게시글 한개 (데이터 베이스 연결 시 삭제 예정)
    private Map<String, Object> sampleBoard(Long id) {
        return Map.of("id", id,
                "title", id + "번째 글",
        "content", "임시 내용.....",
                "username", "김민수");
    }



}
