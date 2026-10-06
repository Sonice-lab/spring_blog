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



}
