package com.tenco.spring_blog.stos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * {
 *   "userId": 1,
 *   "id": 10,
 *   "title": "illo est ratione doloremque quia maiores aut",
 *   "completed": true
 * }
 */

@Data
// 외부 서버에서는 일반적으로 우리 서버에 불필요한 내용까지 많은 데이터를 전달한다.
// 불필요한 필드 즉, 선언이 안된(우리코드) 데이터를 무시하는 명령어
//(안전상의 이유로 선언을 권장)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TodoDto {
    // JSON의 키 값은 키 이름과 자바 필드 이름이 같아야 값이 들어온다.
    private Integer userId;
    private Integer id; // Long도 가능
    private String title;
    private boolean completed; // String도 가능

}
