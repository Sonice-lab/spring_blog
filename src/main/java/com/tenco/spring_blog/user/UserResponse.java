package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.util.MyDateUtil;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

public class UserResponse {
    //마이페이지 응답DTO
    @Data
    public static class DetailDto {
        private Long id;
        private String username;
        private String email;
        private String profileImage; // 파일이름(없으면 null)
        private String createdAt; // 포맷된 가입일

        public DetailDto(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.profileImage = user.getProfileImage();
            this.createdAt = MyDateUtil.timestampFormat(user.getCreatedAt());
        }
    }


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
        private String profileImage;

        public updateFormDto(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.profileImage = user.getProfileImage();
        }
    }


}
