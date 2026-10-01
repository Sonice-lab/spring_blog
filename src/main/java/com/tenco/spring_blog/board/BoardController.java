package com.tenco.spring_blog.board;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

@Slf4j // 무얼 위한 처리였더라?
@RequiredArgsConstructor //DI처리
@Controller // 이건 뭐더라?

public class BoardController {

    //DI 처리
    // 의존성 주입 처리를 왜 하더라????
    private  final BoardNativeRepository boardNativeRepository;





}
