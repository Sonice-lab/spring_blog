package com.tenco.spring_blog._core.util;


import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception500;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Data
// @Component // IoC <-- 이번에는 처리x
// FileUtil은 상태도 없고, 다른 빈에 의존하지 않는 순수한 도구 함수 모음이기에
// Static 메서드로 활용할 예정이다.
public class FileUtil {

    // 1. 업로드 폴더: 사용자 홈 폴더 아래 blog_uploads (운영체제와 관계없이 동작시킬 예정)
    public static final String UPLOAD_DIR =
            Paths.get(System.getProperty("user.home"), "blog_uploads").toString();

    // 2. 허용하는 이미지 형식과 저장할 확장자 설정
    private static final Map<String, String> IMAGE_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/gif", ".gif",
            "image/webp", ".webp"
    );

    // 3. 이미지 저장: 저장할 파일 이름을 반환 (파일이 없으면 null)
    public static String saveImage(MultipartFile file) {
        // 1. 비즈니스 로직
        // 가. 파일 저장은 선택사항으로 결정할 예정
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 나. 업로드를 한 경우
        //    1) 허용한 이미지의 형식인가?
        if (!isImageFile(file)) {
            throw new Exception400("이미지 파일은 (JPG, PNG, GIF, WEBP)만 업로드 가능합니다.");
        }

        // 다. 겹치지 않는 새 파일 이름 만들기(UUID + 확장자)
        String extension = IMAGE_TYPES.get(file.getContentType());
        String savedFilename = UUID.randomUUID() + extension;

        // 라. 폴더가 없으면 만들고, 파일 저장 시킴
        Path uploadPath = Paths.get(UPLOAD_DIR);
        try {
            Files.createDirectories(uploadPath); // 이미 경로가 있으면 아무 일도 하지 않음
            file.transferTo(uploadPath.resolve(savedFilename)); // 폴더 경로 + 파일 이름 (파일 생성 됨) -> 예외 처리가 포함됨
        } catch (IOException e) {
            log.error("파일 저장 실패: {}", e.getMessage()); // 디테일한 에러 찍음(stack trace)
            throw new Exception500("파일 저장 중 오류가 발생했습니다.");
        }

        return savedFilename;

    }

    // 파일 삭제: 파일 이름이 없거나 파일이 이미 없으면 아무 일도 하지 않음
    public static void deleteFile(String filename) {
        if(filename == null || filename.isBlank()) {
            return; // 아무일도 발생하지 않는다.
        }
        try {
            Files.deleteIfExists(Paths.get(UPLOAD_DIR, filename)); // 파일 삭제
        } catch (IOException e) {
            // 파일 삭제에 실패해고 사용자 요청은 계속 진행(로그만 남김)
            log.warn("파일 삭제 실패: {}", e.getMessage());
        }
    }

    // 편의 메서드 설계: 허용한 이미지 형식인지 확인하는 용도
    public static boolean isImageFile(MultipartFile file) {
        if (file == null || file.getContentType() == null) {
            return false; // 형식 정보가 없으면 허용하지 않음
        }
        return IMAGE_TYPES.containsKey(file.getContentType());
    }


}
