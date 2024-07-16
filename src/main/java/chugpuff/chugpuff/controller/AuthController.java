package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.dto.LoginDTO;
import chugpuff.chugpuff.service.MemberService;
import chugpuff.chugpuff.jwt.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    private final MemberService memberService;

    public AuthController(MemberService memberService) {
        this.memberService = memberService;
    }

    // 로그인
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        String id = loginDTO.getId();
        String password = loginDTO.getPassword();

        try {
            Member member = memberService.authenticate(id, password);

            if (member != null) {
                String token = JwtUtil.generateToken(id);

                return ResponseEntity.ok().body("Login successful. Token: " + token);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect username or password.");
            }
        } catch (UsernameNotFoundException e) {
            // 사용자가 존재하지 않을 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found.");
        } catch (BadCredentialsException e) {
            // 잘못된 자격 증명일 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect username or password.");
        } catch (Exception e) {
            // 그 외의 예외
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login failed: " + e.getMessage());
        }
    }
}
