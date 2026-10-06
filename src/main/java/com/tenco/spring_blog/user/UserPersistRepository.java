package com.tenco.spring_blog.user;


import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;


@RequiredArgsConstructor //생성자 주입
@Repository //IoC
public class UserPersistRepository {
    private final EntityManager em;

    //회원 정보 조회 - 로그인(사용자 이름, 비밀번호 확인)
    public User findByUsernameAndPassword(String username, String password) {
        try{
            // JPQL -> 엔티티매니저에서 처리할 수 없을 때 직접 만들어야 함
            String jpql = "select u from User u where u.username = :username and u.password = :password";
            Query query = em.createQuery(jpql, User.class);
            query.setParameter("username", username);
            query.setParameter("password", password);
            return (User) query.getSingleResult();
        }catch (Exception e) {
            // 일치하는 사용자가 없거나 에러 발생 시 null 반환
            // 로그인 실패를 의미함
            return null;
        }
    }

    //회원가입 기능 만들기
    @Transactional
    public User save(User user) {
        // 비영속 상태에 User 객체를 영속성 컨텍스트에 저장
        em.persist(user);
        // 영속성 컨텍스트가 user 객체를 관리하기 시작
        // persist() 호출 후 객체는 영속 상태가 되고, 트랜잭션 커밋 시점이 INSERT query가 실행됨
        // 자동 생성된 ID와 생성기간이 user 객체에 설정된 상태.
        return user;
    }

    // 사용자명 중복 체크용 조회 메서드(중복 행은 단 건 조회로 설계)
    public User findByUserName(String username) {
        // User . find(); <-- PK로 검색
        // 우리가 필요한 건 username 기반으로 조회해야하기 때문
        // 사전에 만들어지지 않았기 때문에 JPQL 사용
        String jpql = """
                select u from User u where u.username = :username
                """;

        try {

            return em.createQuery(jpql, User.class)
                    .setParameter("username", username)
                    .getSingleResult();

        } catch (Exception e) {
            // 사용자를 찾을 수 없는 경우 null 반환
            return null;
        }


    }


}
