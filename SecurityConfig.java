package com.example.chatbot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 ->
                        oauth2.loginPage("/oauth2/authorization/auth0")
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("https://dev-66d43eflbh8w4gl1.us.auth0.com/v2/logout?client_id=PpmOO1mjq2XWnF1exqtxcDPNjzEIP50h&returnTo=http://localhost:8080")
                        .permitAll()
                );

        return http.build();
    }

}
