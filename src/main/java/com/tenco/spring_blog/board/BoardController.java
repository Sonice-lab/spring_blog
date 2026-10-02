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
    private final BoardPersistRepository boardPersistRepository;

    // GET http://localhost:8080/    ,  http://localhost:8080/board/list
    @GetMapping({"/", "/board/list"})
    public String list(Model model) {

        // 역할과 책임에 따라서 데이터에 접근 > boardNativeRepository
        List<Board> boardList = boardPersistRepository.findAll();
        model.addAttribute("boardList", boardList);
        return "board/list";

    }

    // GET   http://localhost:8080/board/3  상세보기
    @GetMapping({"/board/{id}"})
    public String detail(@PathVariable(name = "id") Long id, Model model) {

        // 조회 기능 만들기(기본키로 조회)
        Board boardEntity = boardPersistRepository.findById(id);
        //Board boardEntity = boardPersistRepository.findByIdWithJPQL(id);
        if (boardEntity == null) {
            // 추후에 404 에러 페이지를 만들어서 처리할 예정
            throw new RuntimeException("게시글을 찾을 수 없습니다.: " + id);
        }

        model.addAttribute("board", boardEntity);

        return "board/detail";
    }

    // GET   http://localhost:8080/board/save  (화면 요청)
    @GetMapping({"/board/save"})
    public String saveForm() {
        // 뼈대용 임시 데이터
        return "board/save-form";
    }


    @PostMapping({"/board/save"})
    //폼 데이터 바인딩 처리 -> 스프링이 HTTP 요청 파라미터를 객체로 자동 변환
    //Spring이 폼 데이터를 객체로 변환하는 과정(데이터 바인딩 메커니즘)
    public String save(BoardRequest.SaveDto reqDto) {

        // 데이터 타입이 다른 이슈 해결방법
        // 1. DTO에서 Entity 클래스 타입으로 변환해주어야 한다.
        // 비영속 상태
        Board board = Board.builder()
                .title(reqDto.getTitle())
                .content(reqDto.getContent())
                .username(reqDto.getUsername())
                .build();
        // new Board(reqDto.getTitle(), reqDto.getContent(), reqDto.getUsername());
        // 방어 로직 작동: 데이터베이스에 접근하거나 엔티티를 만들기 전에 필수 값을 먼저 검사
        // 여기서 빈 값이 발견되면 우리가 DTO에 정의한 IllegalAugumentException에 발생하며 아래 코드는 실행되지 않음
        reqDto.validate();

        //
        Board boardEntity = boardPersistRepository.save(board);

        return "redirect:/";
    }

    // GET   http://localhost:8080/board/1/update  (수정 화면 요청)
    @GetMapping({"/board/{id}/update"})
    public String updateForm(@PathVariable Long id, Model model) {

        // 수정하기 화면 요청(먼저 조회부터)
        Board board = boardPersistRepository.findById(id);
        model.addAttribute("board", board);

        return "board/update-form";
    }

    // POST   http://localhost:8080/board/1/update  (게시글 수정 기능 요청)
    // object를 통으로 받기
    @PostMapping({"/board/{id}/update"})
    public String update(@PathVariable Long id,
                         BoardRequest.UpdateDto reqDto) {
        reqDto.validate(); // 유효성 검사에서 걸린다면? -> throw로 던져짐

        boardPersistRepository.updateById(id, reqDto);
        //PRG 패턴 구현
        return "redirect:/board/" + id; //리다이렉트 수정된 게시글 상세보기 화면 이동
    }

    // 게시글 삭제
    // 주소 설계
    @PostMapping("/board/{id}/delete")
    public String delete(@PathVariable Long id) {
        boardPersistRepository.deleteById(id);
        //PRG 패턴 활용
        return "redirect:/";
    }
}
