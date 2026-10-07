package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.Exception400;
import lombok.Data;

public class UserRequest {

    // 회원정보 수정용 DTO 설계
    @Data
    public static class UpdateDto {
        private String password;

        public void Validate() {
            if (password == null || password.trim().isEmpty()) {
                throw new Exception400("비밀번호는 필수입니다.");
            }
            if (password.length() < 4) {
                throw new Exception400("비밀번호는 네글자이상이어야 합니다.");
            }
            //필요하다면 길이 수 제한, 특수문자 포함 여부 설정 (정규표현식) 활용 가능
        }
        // toEntity(); ->save일 때만 필요. 그렇기에 필요 없음 (더티체킹을 활용하기 때문에)
    }

    // 회원가입용 DTO 설계
    @Data
    public static class JoinDto {
        private String username;
        private String password;
        private String email;


        // 회원가입 시 데이터 검증 메서드를 똑같이 추가하기
        public void Validate() {
            if (username == null || username.trim().isEmpty()) {
                throw new Exception400("사용자명은 필수입니다.");
            }

            if (password == null || password.trim().isEmpty()) {
                throw new Exception400("패스워드는 필수입니다.");
            }

            if (email == null || email.trim().isEmpty()) {
                throw new Exception400("이메일은 필수입니다.");
            }

            // 간단하게 이메일 형식만 만들어보자.
            if (!email.contains("@")) {
                throw new Exception400("올바른 이메일 형식이 아닙니다.");
            }
        }

        // DTO에서 User 엔티티로 변환하는 메서드
        // DTO는 계층간의 데이터 전달을 의미
        // 계층간의 데이터 변환을 명확하게 분리하는 것이 좋음
        // JoinDto에서 메서드를 호출하면 User 객체를 반환하는 코드를 작성하기

        //toEntity -> 전부 설계한 후 Controller 설계하기
        public User toEntity() {
            // 만약 JoinDto의 정보가 전부 들어왔다면 User 객체로 반환해야한다.
            return User.builder()
                    .username(username)
                    .password(password)
                    .email(email)
                    .build();
        }
    } //end of joinDto

    @Data
    public static class LoginDto {
        private String username;
        private String password;

        // 회원가입 시 데이터 검증 메서드를 똑같이 추가하기
        public void Validate() {
            if (username == null || username.trim().isEmpty()) {
                throw new Exception400("사용자명은 필수입니다.");
            }

            if (password == null || password.trim().isEmpty()) {
                throw new Exception400("패스워드는 필수입니다.");
            }
        }
    }

}
