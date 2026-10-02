package com.tenco.spring_blog.board;


import lombok.Data;

//DTO 클래스 개념
// =================================================================
// [1단계] DTO 그룹화 (Wrapper 클래스 설계)
// =================================================================
// 1. 게시글과 관련된 '요청(Request)'용 DTO 클래스들을 한곳에 모아두기 위한 큰 껍데기(아우터) 클래스를 만든다.
// 2. 이렇게 모아두면 나중에 사용할 때 'BoardRequest.SaveDto'처럼 소속이 명확해져 유지보수하기 좋다.
public class BoardRequest {


    // =================================================================
    // [2단계] 게시글 저장(Create) 요청용 DTO 설계
    // =================================================================
    // 1. 클래스 목적: 사용자가 새 글을 작성할 때 화면에서 넘어오는 데이터를 담는 그릇.
    // 2. 클래스 형태: 외부 클래스를 굳이 생성하지 않아도 바로 쓸 수 있도록 '정적(static) 내부 클래스'로 만든다.
    // 3. 롬복(Lombok) 설정: Getter, Setter, toString 등을 알아서 만들어주는 어노테이션을 붙인다.
    // 4. 필드 정의: 화면에서 입력받는 데이터(제목, 내용, 작성자)를 캡슐화(private)하여 선언한다.

    @Data
    public static class SaveDto {
        private String title;
        private String content;
        private String username;


        // =================================================================
        // [2-1단계] 저장 데이터 유효성 검증(Validation) 로직 만들기
        // =================================================================
        // 1. 메서드 목적: 필수 데이터가 비어있는 상태로 DB까지 넘어가는 것을 막기 위한 1차 방어선.
        public void validate() {
            // 2. [작성자 검증]: 작성자 값이 아예 없거나(null) 공백만 있다면 에러(Exception)를 터뜨린다.
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("이름은 필수입니다.");
            }
            // 3. [제목 검증]: 제목 값이 없거나 공백만 있다면 에러를 터뜨린다.
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("제목은 필수입니다.");
            }
            // 4. [내용 검증]: 내용 값이 없거나 공백만 있다면 에러를 터뜨린다.
            if (content == null || content.trim().isEmpty()) {
                throw new IllegalArgumentException("내용은 필수입니다.");
            }
            // (힌트: 문자열의 앞뒤 공백을 잘라내는 trim()과 비어있는지 확인하는 isEmpty()를 활용해 본다.)
        }
    }


    // =================================================================
    // [3단계] 게시글 수정(Update) 요청용 DTO 설계
    // =================================================================
    // 1. 클래스 목적: 사용자가 기존 글을 수정할 때 화면에서 넘어오는 데이터를 담는 그릇.
    // 2. 클래스 형태: 마찬가지로 '정적(static) 내부 클래스'로 만든다.
    // 3. 롬복(Lombok) 설정: Getter, Setter 등을 자동 생성해주는 어노테이션을 붙인다.

    @Data
    public static class UpdateDto {

        // 4. 필드 정의: 수정할 때 변경이 허용되는 데이터(일반적으로 제목, 내용)만 선언한다. (작성자는 제외)
        private String title;
        private String content;


        // =================================================================
        // [3-1단계] 수정 데이터 유효성 검증(Validation) 로직 만들기
        // =================================================================
        // 1. 메서드 목적: 필수 수정 데이터가 올바르게 들어왔는지 확인한다.
        public void validate() {

            // 2. [제목 검증]: 새로 넘어온 제목이 없거나 공백만 있다면 에러를 터뜨린다.
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("제목은 필수입니다.");
            }
            // 3. [내용 검증]: 새로 넘어온 내용이 없거나 공백만 있다면 에러를 터뜨린다.
            if (content == null || content.trim().isEmpty()) {
                throw new IllegalArgumentException("내용은 필수입니다.");
            }
        }
    }
}
