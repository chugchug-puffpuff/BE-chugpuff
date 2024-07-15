package chugpuff.chugpuff.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/login", "/members", "/members/checkUserId").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화
                .formLogin(form -> form.disable()) // 기본 폼 로그인 비활성화
                .httpBasic(withDefaults()) // HTTP Basic 인증 활성화
                .rememberMe(rememberMe -> rememberMe
                        .key("uniqueAndSecret")
                        .tokenValiditySeconds(604800) // 7일 동안 유지
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
