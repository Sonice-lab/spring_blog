package com.tenco.spring_blog.admin;

import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.Role;
import com.tenco.spring_blog.user.SessionUserDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import static com.tenco.spring_blog.user.Role.ADMIN;

@Controller
public class AdminController {
    // GET http://localhost:8080/admin/dashboard
    @GetMapping("/admin/dashboard")
    public String dashBoard(Model model, HttpSession session) {

        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        // ADMIN만 접근할 수 있도록 설계
        if(!sessionUserDto.isAdmin()) {
            throw new Exception403("권한이 없습니다.");
        }
        model.addAttribute("adminName", sessionUserDto.getUsername());
        return "admin/dashboard";
    }
}
