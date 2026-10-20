package com.tenco.spring_blog.stos;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController // @Controller + @ReponseBody -> 데이터를 내려준다.
public class RestTemplateController {

    // 서버에서 다른 서버로 실행시키기 위한 시점은?
    // 웹브라우저에서 주소를 치면 다른 서버로 간다.

    // 테스트 주소 = http://localhost:8080/practice/todos/10
    @GetMapping("/practice/todos/{id}")
    public ResponseEntity<?> getTodoAsString(@PathVariable(name = "id") Integer id) {

        // 1. 요청할 외부 API 주소 -> https://jsonplaceholder.typicode.com/todos/1
        // jsonplaceholder 에서 가져옴
        String url = "https://jsonplaceholder.typicode.com/todos/" + id;

        // 2. HTTP 요청을 보내는 도구 생성
        // RestTemplate -> HTTP 메세지를 만들어준다.
        RestTemplate restTemplate = new RestTemplate();

        // 3. 요청 헤더 - 응답을 JSON으로 해주세요.
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON)); // 0번째 인덱스에 들어갈 내용

        // 3번에 대한 내용은 왜 알아야할까? -> 추후 외부 API 사용 시 헤더에 '어떤식으로 데이터를 던져라'라는 양식이 문서로 나와 있을 경우
        // 검토하여 맞춰서 생성하라는 명령

        // 4. 요청 꾸러미(HttpEntity) = 헤더 + 바디(HTTP 요청 메세지 헤더와 바디를 만들어준다.) -> 응답 HTTP 메서드로 인지하기
        HttpEntity request = new HttpEntity<>(headers);
        // GET - 자원의 요청(바디가 없음)

        //-------------- 여기까지 조립 끝!

        // 5. 요청 보내기 - exchange(주소, 메서드, HTTP 메세지(요청 꾸러미), 응답 바디 타입)
        // 타입 다운캐스팅 반영
        // 응답에서 필요한 부분 가공하기
        ResponseEntity<TodoDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                TodoDto.class // 어떤 데이터 형식으로 받을 것인지
        );

        // 6. 응답 확인하기
        System.out.println("상태 코드 확인: " + response.getStatusCode());
        System.out.println("응답 헤더 확인: " + response.getHeaders());
        System.out.println("바디 내용 확인: " + response.getBody());

        // 7. 응답 DTO 형태 구축
        System.out.println("userId : " + response.getBody().getUserId());
        System.out.println("id: " + response.getBody().getId());
        System.out.println("title: " + response.getBody().getTitle());
        System.out.println("completed: " + response.getBody().isCompleted());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response.getBody());
    }

    // 브라우저 -> 우리 서버 요청 주소 설정
    // 테스트 주소 = http://localhost:8080/practice/posts
    @GetMapping("/practice/posts")
    public ResponseEntity<?> createPost() {

        // 1. 요청할 외부 API 주소
        // jsonplaceholder 에서 가져옴 -> http://localhost:8080/practice/posts
        String url = "https://jsonplaceholder.typicode.com/posts";

        // 2. HTTP 요청을 보내는 도구 생성
        // RestTemplate -> HTTP 메세지를 만들어준다.
        RestTemplate restTemplate = new RestTemplate();

        // 3. 요청 헤더 - 응답을 JSON으로 해주세요.
        // 중요! - Content-Type: "보내는 바디는 JSON이다."
        // Accept: "응답은 JSON으로 주세요."
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        // --------------- 여기까지 헤더 구성 완료!

        // 4. 보낼 데이터 구성
        PostDto postDto = PostDto.builder()
                .userId(100)
                .title("오늘은 StoS를 배우는 날입니다.")
                .body("살려주세요.")
                .build();

        // 5. HTTPEntity 만들기 -> 바디 + 헤더
        HttpEntity<PostDto> request = new HttpEntity<>(postDto, headers);

        try {

            // 6.
            ResponseEntity<PostDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    PostDto.class // 어떤 데이터 형식으로 받을 것인지
            );

            // DTO - 하나로 요청과 응답으로 활용
            // 요즘 코드는 요청 DTO, 응답 DTO 설계가 관례
            System.out.println(response.getStatusCode());
            System.out.println(response.getHeaders());
            System.out.println(response.getBody());

            return ResponseEntity.ok().body(response.getBody());

        } catch (HttpClientErrorException e) {
            // 외부 서버가 보낸 상태 코드와 오류 내용을 확인할 수 있다.
            return ResponseEntity.status(e.getStatusCode()).body("외부 API 요청에 실패했습니다.");
        }
    }

    // 외부와 통신하는 코드에는 서버가 안 터질 수 있도록 코드는 raw하게 구현하는 것이 좋다.

}
