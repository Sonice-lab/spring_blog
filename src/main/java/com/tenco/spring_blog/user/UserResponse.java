package com.tenco.spring_blog.user;

import lombok.Data;

public class UserResponse {

    // 1. 회원가입 응답 DTO
    @Data
    public static class JoinDto {
        private Long id;
        private String username;
        private String email;

        public JoinDto(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
        }
    }

    // 2. 회원정보 수정 화면 응답 DTO
    @Data
    public static class updateFormDto {
        private Long id;
        private String username;
        private String email;

        public updateFormDto(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
        }
    }

    


}
