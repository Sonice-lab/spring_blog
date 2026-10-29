package com.tenco.spring_blog.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {

    //todo - ... 추후 필요한 부분이 있다면 설계
    // 조회 기능
    @Query("select p from Payment p where p.paymentId = :paymentId")
    Optional<Payment> findByPaymentId(@Param("paymentId") String paymentId);

    // 쿼리 메서드
    @Query("""
            select case when count(p) > 0 then true else false end
            from Payment p
            where p.paymentId = :paymentId""")
    boolean existsByPaymentId(@Param("paymentId") String paymentId);

    
    // 결제 내역 조회 (로그인 사용자)
    // 상태값(CANCELLED, PAID)만 받도록 함 -> in 쿼리로 구현
    @Query("""
            select p from Payment p
            where p.user.id = :userId
            and p.status in :statusList 
            order by p.id desc
            """)
    List<Payment> findAllByUserIdAndStatusList(@Param("userId") Long userId,
                                               @Param("statusList") List<PaymentStatus> statusList);


}
