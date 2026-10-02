package com.tenco.spring_blog.board;

// 화면(HTML)이 아니라 데이터를 주고 받는 컨트롤러
// 주소 앞에 /api는 화면 주소와 데이터 주소를 구분하기 위해 사용한다.

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

// 데이터를 주고 받으려면 RestController를 사용해야 한다.
@RestController // @Controller +@ResponseBody를 의미
@RequiredArgsConstructor
public class BoardApiController {

    private final BoardPersistRepository boardPersistRepository;


    // 주소 설계
    // DELETE http://localhost:8080/api/boards/{id}
    @DeleteMapping("/api/boards/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        try {
            boardPersistRepository.deleteByID(id);
            return ResponseEntity.ok("정상 삭제 되었습니다."); // 200 + 메세지
        }catch (Exception e) {
            return ResponseEntity.badRequest().body("잘못된 요청입니다."); // 400 + 메세지
        }
    }
}
