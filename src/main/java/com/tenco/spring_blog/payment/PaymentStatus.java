package com.tenco.spring_blog.payment;

public enum PaymentStatus {
    READY("결제 대기"), // 결제 예정
    PAID("결제 완료"),// 결제 확인 및 포인트 지급 완료
    CANCELLED("환불 완료");// 결제 확인 및 포인트 지급 완료


    // 반드시 세미콜론을 붙여주어야 한다.

    // 화면에 표시할 이름 변수 정보
    private final String displayName;


    private PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }


}
