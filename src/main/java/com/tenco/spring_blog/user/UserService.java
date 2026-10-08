package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception404;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사전 기반 지식
 * 역할과 책임을 분리하는 방식
 * 서비스 레이어
 * 핵심 개념:
 * - 비즈니스 로직을 처리하는 계층
 * - Controller와 Repository 사이의 중간 계층을 담당한다.
 * - 서비스에서 트랜잭션 관리에 대한 부분도 같이 처리한다.
 * 이와 같은 부분은 3Tier 아키텍처
 * C -> S -> R -> DB
 */


@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true) //기본적으로 읽기 전용 트랜잭션 설정
@Service
public class UserService {

    private final UserJpaRepository userJpaRepository;  //DI 주입 완료

    /**
     * 회원가입 처리
     *
     * @param joinDto 회원 가입 정보
     * @return 저장된 사용자 정보
     */
    // 회원가입 기능
    @Transactional // 쓰기 작업이므로 트랜잭션 활성화
    public User join(UserRequest.JoinDto joinDto) {
        log.info("회원 가입 서비스 처리 시작 - 사용자명: {}", joinDto.getUsername());

        // 비즈니스 조직
        // 1. 중복체크하기 - 이미 있으면 null이 아님(idPresent 차이점 확인)
        userJpaRepository.findByUsername(joinDto.getUsername())
                .ifPresent(user -> {
                    log.warn("회원가입 실패 - 중복된 사용자명: {}", joinDto.getUsername());
                    throw new Exception400("이미 존재하는 사용자명입니다.");
                });

        // 2. 데이터 저장
        User savedUser = userJpaRepository.save(joinDto.toEntity());
        log.info("회원 가입 서비스 처리 완료 - 사용자명: {}, ID: {} ",
                joinDto.getUsername(), savedUser.getId());
        return savedUser;
    }

    /**
     * 로그인 처리
     *
     * @param loginDto 로그인 정보
     * @return 로그인된 사용자 정보
     */
    // 로그인 처리 - 조회
    public User login(UserRequest.LoginDto loginDto) {
        log.info("로그인 서비스 처리 시작 - 사용자명: {}", loginDto.getUsername());

        User user = userJpaRepository.findByUsernameAndPassword(
                        loginDto.getUsername(), loginDto.getPassword())
                .orElseThrow(() -> {
                    log.warn("로그인 실패 - 사용자명: {}, 원인: 인증정보 불일치", loginDto.getUsername());
                    return new Exception400("사용자명 또는 비밀번호가 올바르지 않습니다.");
                });

        log.info("로그인 성공 - 사용자명: {}", loginDto.getUsername());
        return user;
    }

    /**
     * 사용자 정보 조회
     * @param id 사용자 pk
     * @return 사용자 정보
     */
    // 사용자 정보 조회 기능
    public User findById(Long id) {
        log.info("사용자 정보 조회 - id: {}", id);
        return userJpaRepository.findById(id).orElseThrow(() ->
                new Exception404("사용자를 찾을 수 없습니다."));
    }

    /**
     * 회원정보 수정처리(Dirty Checking 활용)
     * @param id 사용자 pk
     * @param updateDto 수정할 사용자 정보
     * @return 수정된 사용자 정보
     */
    // 사용자 정보 수정 기능
    @Transactional
    public User updateById(Long id, UserRequest.UpdateDto updateDto) {
        // 유저 조회
        User user = findById(id);
        // 더티체킹
        user.update(updateDto.getPassword());
        return user;
    }
}
