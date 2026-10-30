package com.tenco.spring_blog.refund;

import com.tenco.spring_blog.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    // 결제 (payment.id) 로 조회 기능 추가
    @Query("select r from Refund r where r.payment.id = :paymentPk")
    Optional<Refund> findByPaymentPk(@Param("paymentPk") Long paymentPk);

    // 사용자가 환불 요청한 결제 PK 목록 열람(결제 내역 화면에 환불 버튼 표시로 사용됨)
    @Query("select r.payment.id from Refund r where r.user.id = :userId")
    List<Long> findPaymentPkListByUserId(@Param("userId") Long userId);

    // 로그인한 사용자의 환불 요청 내역 리스트 (결제 정보 함께 조회, 최신순으로 정렬)
    @Query("""
                select r from Refund r
                join fetch r.payment
                where r.user.id = :userId
                order by r.id desc
            """)
    List<Refund> findAllByUserIdJoinPayment(@Param("userId") Long userId);



    // todo
    // 관리자: 전체 환불 요청 목록
    @Query("""
                select r from Refund r
                join fetch r.user
                join fetch r.payment
                order by r.id desc
            """)
    List<Refund> findAllJoinUserAndPayment();

    // 관리자: 승인할 환불 요청 1건 정보 조회
    @Query("""
            select r from Refund r
            join fetch r.user
            join fetch r.payment
            where r.id = :id
            """)
    Optional<Refund> findByIdJoinUserAndPayment(@Param("id") Long id);

}
