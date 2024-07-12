package chugpuff.chugpuff.security;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.service.CustomUserDetails;
import chugpuff.chugpuff.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/login", "/members", "/members/checkUserId").permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin(formLogin ->
                        formLogin
                                // 로그인 설정
                                .defaultSuccessUrl("/home", true)
                                .failureUrl("/login?error")
                                .permitAll()
                )
                .httpBasic()
                .and()
                .logout(logout ->
                        logout
                                // 로그아웃 설정
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/login?logout")
                                .permitAll()
                )
                .rememberMe(rememberMe ->
                        rememberMe
                                // 로그인 유지 설정
                                .key("uniqueAndSecret")
                                .tokenValiditySeconds(604800) // 7일 동안 유지
                )
                .csrf(csrf ->
                        csrf.disable() // CSRF 보호 비활성화
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
