package chugpuff.chugpuff.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    // 로그인
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
