package com.tenco.spring_blog.payment;

import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.SessionUserDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PaymentApiController {

    private final PortOnePaymentService portOnePaymentService;

    // /api/payment/prepare
    // 요청 바디 ("amount" : 5000)
    @PostMapping("/api/payment/prepare")
    public ResponseEntity<?> prepare(@RequestBody PaymentRequest.PrepareDto prepareDto, HttpSession session) {

        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        if(sessionUserDto == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        }
        // 유효성 검사
        prepareDto.validate();

        // 서비스단 위임
        return ResponseEntity.ok(portOnePaymentService.prepare(sessionUserDto.getId(), prepareDto.getAmount()));
    }

    // /api/payment/complete
    // 요청 바디 ("paymentId" : 5000)
    @PostMapping("/api/payment/complete")
    public ResponseEntity<?> complete(@RequestBody PaymentRequest.CompleteDto completeDto, HttpSession session) {

        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        if(sessionUserDto == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        }
        // 유효성 검사
        completeDto.validate();

        // 서비스단 위임
        return ResponseEntity.ok(portOnePaymentService.complete(sessionUserDto.getId(), completeDto.getPaymentId()));
    }
}
