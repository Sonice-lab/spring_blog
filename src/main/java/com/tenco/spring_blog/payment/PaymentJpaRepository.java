package com.tenco.spring_blog.payment;

import com.tenco.spring_blog.purchase.Purchase;
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

    // 5. 로그인 사용자 결제 내역 조회
    // 가져와야할 것 : 결제 일련번호 + 주문번호 + 결제번호 + 결제일 + 결제 상태(대기, 완료, 환불 등)을 한 번에 쿼리로 가져와야 한다.(최신 구매순 정렬)
    @Query("""
            select p from Payment p 
            join fetch p.user u
            where p.user.id = :userId
            order by p.id desc 
            """)
    List<Payment> findAllByUserIdPayment(@Param("userId") Long userId);

}
