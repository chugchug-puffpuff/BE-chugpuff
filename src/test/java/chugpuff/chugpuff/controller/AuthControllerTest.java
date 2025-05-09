package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.dto.LoginDTO;
import chugpuff.chugpuff.jwt.JwtUtil;
import chugpuff.chugpuff.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginDTO loginDTO;
    private Member member;
    private String validToken;
    private String invalidToken;

    @BeforeEach
    public void setUp() {
        loginDTO = new LoginDTO();
        loginDTO.setId("test_user");
        loginDTO.setPassword("password");

        member = new Member();
        member.setId("test_user");
        member.setPassword("encodedPassword");

        validToken = JwtUtil.generateToken("test_user");
        invalidToken = "InvalidToken";
    }

    @Test
    @DisplayName("로그인 성공")
    public void testLoginSuccess() throws Exception {
        Mockito.when(memberService.authenticate("test_user", "password")).thenReturn(member);

        MvcResult result = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Login successful. Token: "));
        System.out.println("Response: " + responseContent);
        System.out.println("로그인 성공");
    }

    @Test
    @DisplayName("로그인 실패 - 사용자 없음")
    public void testLoginUserNotFound() throws Exception {
        Mockito.when(memberService.authenticate("test_user", "password")).thenThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not found."))
                .andDo(print());
        System.out.println("로그인 실패 - 사용자 없음");
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 자격 증명")
    public void testLoginBadCredentials() throws Exception {
        Mockito.when(memberService.authenticate("test_user", "password")).thenThrow(new BadCredentialsException("Incorrect username or password"));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Incorrect username or password."))
                .andDo(print());
        System.out.println("로그인 실패 - 잘못된 자격 증명");
    }

    @Test
    @DisplayName("로그아웃 성공")
    public void testLogoutSuccess() throws Exception {
        mockMvc.perform(post("/logout")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNoContent());
        System.out.println("로그아웃 성공");
    }
}
