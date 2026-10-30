package com.tenco.spring_blog.admin;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.refund.PortOneRefundService;
import com.tenco.spring_blog.refund.RefundResponse;
import com.tenco.spring_blog.refund.RefundService;
import com.tenco.spring_blog.user.Role;
import com.tenco.spring_blog.user.SessionUserDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static com.tenco.spring_blog.user.Role.ADMIN;


@Slf4j
@RequiredArgsConstructor
@Controller
public class AdminController {

    private final RefundService refundService;
    private final PortOneRefundService portOneRefundService;

    // GET http://localhost:8080/admin/dashboard
    @GetMapping("/admin/dashboard")
    public String dashBoard(Model model, HttpSession session) {

        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);

        model.addAttribute("adminName", sessionUserDto.getUsername());
        return "admin/dashboard";
    }

    // GET http://localhost:8080/admin/refund/list (관리자 환불 요청 관리)
    @GetMapping("/admin/refund/list")
    public String refundList(Model model) {

        // 데이터베이스에서 조회 내려주기
        List<RefundResponse.AdminListDto> refundList = refundService.findAllForAdmin();
        model.addAttribute("refundList", refundList);
        return "admin/refund-list";
    }

    // 관리자 환불 요청 거절
    // POST http://localhost:8080/admin/refund/{id}/reject
    // -H Content-Type : x-www-form-urlencoded -> 일반폼
    @PostMapping("/admin/refund/{id}/reject")
    public String rejectRefund(@PathVariable(name = "id") Long id,
                               @RequestParam(name = "rejectReason") String rejectReason) {


        // 1. 거절 사유 입력
        if (rejectReason == null || rejectReason.trim().isEmpty()) {
            throw new Exception400("거절 사유를 반드시 입력");
        }
        // 2. 거절 사유는 500자 이내로 작성해야 함
        if (rejectReason.length() > 500) {
            throw new Exception400("거절 사유는 500자 이하로 입력해주세요.");
        }

        refundService.reject(id, rejectReason);

            return "redirect:/admin/refund/list";

    }

    // POST http://localhost:8080/admin/refund/{id}/approve (환불 승인: 포트원 결제 취소)
    // -H Content-Type : x-www-form-urlencoded -> 일반폼
    @PostMapping("/admin/refund/{id}/approve")
    public String approvaRefund(@PathVariable Long id) {
        // 호출 먼저 진행
        portOneRefundService.approve(id);
        return "redirect:/admin/refund/list";
    }


}
