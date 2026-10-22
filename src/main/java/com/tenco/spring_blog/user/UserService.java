package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog._core.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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

// 엔티티 DTO로 내리기 위한 수정 완료
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true) //기본적으로 읽기 전용 트랜잭션 설정
@Service
public class UserService {

    private final UserJpaRepository userJpaRepository;  //DI 주입 완료
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입 처리
     *
     * @param joinDto 회원 가입 정보
     * @return 저장된 사용자 정보
     */
    // 회원가입 기능
    @Transactional // 쓰기 작업이므로 트랜잭션 활성화
    public UserResponse.JoinDto join(UserRequest.JoinDto joinDto) {
        log.info("회원 가입 서비스 처리 시작 - 사용자명: {}", joinDto.getUsername());

        // 비즈니스 조직
        // 1. 중복체크하기 - 이미 있으면 null이 아님(ifPresent 차이점 확인)
        userJpaRepository.findByUsername(joinDto.getUsername())
                .ifPresent(user -> {
                    log.warn("회원가입 실패 - 중복된 사용자명: {}", joinDto.getUsername());
                    throw new Exception400("이미 존재하는 사용자명입니다.");
                });

        // 2. 이메일 중복 검사
        userJpaRepository.findByEmail(joinDto.getEmail())
                .ifPresent(user -> {
                    log.warn("회원가입 실패 - 중복된 이메일: {}", joinDto.getEmail());
                    throw new Exception400("이미 사용중인 이메일입니다.");
                });

        String profileImage = FileUtil.saveImage(joinDto.getProfileImage());

        // 3. 비밀번호 해싱 처리
        String encodePassword = passwordEncoder.encode(joinDto.getPassword()); //  평문이 해싱으로 변환

        // 4. 프로필 이미지 저장 (새롭게 생긴 파일 이름이 리턴됨) -> 비영속 상태
        User user = joinDto.toEntity(profileImage, encodePassword);

        // Role 기본값 할당
        user.addRole(Role.USER);

        // 5. 프로필 파일 이름과 회원 정보 저장
        User savedUser = userJpaRepository.save(user);
        log.info("회원 가입 서비스 처리 완료 - 사용자명: {}, ID: {} ",
                joinDto.getUsername(), savedUser.getId());
        return new UserResponse.JoinDto(savedUser);
    }

    /**
     * 로그인 처리
     *
     * @param loginDto 로그인 정보
     * @return 로그인된 사용자 정보
     */
    // 로그인 처리 - 조회
    public SessionUserDto login(UserRequest.LoginDto loginDto) {
        log.info("로그인 서비스 처리 시작 - 사용자명: {}", loginDto.getUsername());

        // 1. 사용자명으로만 조회(역할 포함)
        User user = userJpaRepository.findByUsernameWithRoles(
                        loginDto.getUsername())
                .orElseThrow(() -> {
                    log.warn("로그인 실패 - 사용자명: {}, 원인: 인증정보 불일치", loginDto.getUsername());
                    return new Exception400("사용자명 또는 비밀번호가 올바르지 않습니다.");
                });

        // 2. 비밀번호 비교: matches(입력한 평문, DB에 저장된 해시)
        if(!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            log.warn("로그인 실패");
            throw new Exception400("사용자명 또는 비밀번호가 올바르지 않습니다."); // 유추할 수 없도록 설계(보안상의 로직)
        }


        log.info("로그인 성공 - 사용자명: {}", loginDto.getUsername());
        return new SessionUserDto(user);
    }

    /**
     * 회원 정보 수정 화면:  화면에 필요한 값만 담는 DTO 반환
     *
     * @param userId
     * @return
     */
    public UserResponse.updateFormDto findUpdateForm(Long userId) {
        User user = findById(userId);
        return new UserResponse.updateFormDto(user);
    }


    /**
     * 회원정보 수정처리(Dirty Checking 활용)
     *
     * @param id        사용자 pk
     * @param updateDto 수정할 사용자 정보
     * @return 수정된 사용자 정보
     */
    // 사용자 정보 수정 기능
    // 세션 동기화용 DTO 반환
    @Transactional
    public SessionUserDto updateById(Long id, UserRequest.UpdateDto updateDto) {
        // 1. 유저 조회
        User user = findById(id);

        // 2. 더티체킹 -> 비밀번호 조회
        if (updateDto.hasPassword()) {
            user.update(passwordEncoder.encode(updateDto.getPassword()));
        }

        // 이미지 변경 -> 새 이미지를 골랐을 때만 교체
        // null이거나, 파일 데이터가 있거나 두 가지 경우가 있다.
        if (updateDto.hasProfileImage()) {
            String newImage = FileUtil.saveImage(updateDto.getProfileImage());
            String oldImage = user.getProfileImage();
            user.updateProfileImage(newImage); // DB에는 새 파일 이름 변경 감지
            // 기존 파일은 서버측 폴더에서 삭제 처리
            FileUtil.deleteFile(oldImage);
        }

        // 4. 세션 동기화용 DTO 변환
        return new SessionUserDto(user);
    }


    /**
     * 사용자 정보 조회
     *
     * @param id 사용자 pk
     * @return 사용자 정보
     */
    // 사용자 정보 조회 기능
    // 캡슐화 - 서비스 안에서만 쓰는 조회용 메서드
    private User findById(Long id) {
        log.info("사용자 정보 조회 - id: {}", id);
        return userJpaRepository.findById(id).orElseThrow(() ->
                new Exception404("사용자를 찾을 수 없습니다."));
    }

    // 마이페이지 조회 기능
    // 조회라서 트랜잭션 걸 필요 없음
    public UserResponse.DetailDto findDetail(Long userId) {
        User user = findById(userId);
        return new UserResponse.DetailDto(user);
    }

    @Transactional
    public void deleteProfileImage(Long userId) {
        User user = findById(userId);
        String oldImage = user.getProfileImage();
        user.updateProfileImage(null); // 1. DB에 파일 이름 비우기(변경 감지)
        FileUtil.deleteFile(oldImage); // 2. 폴더 실제 파일 삭제

    }

    // 카카오 로그인: 처음이면 자동 회원가입, 이미 있으면 로그인
    @Transactional
    public SessionUserDto kakaoLogin(UserResponse.KakaoUserInfo kakaoUserInfo) {
        // 1. 카카오 회원번호로 우리 서비스에 사용자명을 자동으로 만들어준다.
        String username = "kakao_" + kakaoUserInfo.getId();

        // 2. 이미 가입했는지 확인, 없으면 자동 회원가입
        User user = userJpaRepository.findByUsername(username)
                .orElseGet(() -> joinKakaoUser(username, kakaoUserInfo));

        // 3. 혹시라도 같은 이름(동명이인)의 일반 회원이 있다면 카카오 사용자로 로그인 시키지 않음
        if (user.getProvider() != OAuthProvider.KAKAO) {
            throw new Exception400("이미 사용중인 사용자명입니다.");
        }
        // 트랜잭션 안에서 SessionUserDto 생성
        return new SessionUserDto(user);
    }

    private User joinKakaoUser(String username, UserResponse.KakaoUserInfo kakaoUserInfo) {
        User newUser = User.builder()
                .username(username)
                // 일반 로그인? 카카로 로그인?
                // 카카오 사용자는 비밀번호로 로그인하지 않으므로 아무도 알 수 없는 값을 입력
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .profileImage(kakaoUserInfo.getProperties().getProfileImage())
                .provider(OAuthProvider.KAKAO)
                .email(username + "@kakao.com")
                .build();

        newUser.addRole(Role.USER);

        return userJpaRepository.save(newUser);
    }

    // 이메일 중복 체크 -> 이메일 사용 가능 여부 확인
    public void checkEmailAvailable(String email) {
        userJpaRepository.findByEmail(email)
                .ifPresent(user -> {
                    throw new Exception400("이미 가입되어있는 이메일입니다.");
                });
    }
}
