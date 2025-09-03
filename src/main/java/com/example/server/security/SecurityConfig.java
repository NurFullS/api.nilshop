package com.example.server.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Отключаем CSRF для API
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/auth/**", "/css/**", "/js/**", "/images/**").permitAll() // Разрешаем публичные эндпоинты
                .anyRequest().permitAll() // Остальное тоже разрешаем (без авторизации)
            )
            .formLogin(form -> form.disable()) // Убираем дефолтную форму логина
            .httpBasic(httpBasic -> httpBasic.disable()) // Убираем Basic Auth
            .logout(logout -> logout.disable()); // Убираем стандартный logout

        return http.build();
    }
}
