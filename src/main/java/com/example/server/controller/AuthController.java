package com.example.server.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.server.modal.Auth;
import com.example.server.repository.AuthRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {

    private final AuthRepository authRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final String jwtSecret = "mySuperSecretKey12345678901234567890";
    private final long JWT_EXPIRATION = 7 * 24 * 60 * 60 * 1000;

    public AuthController(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Auth user) {
        try {
            if (authRepository.existsByEmail(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email уже зарегистрирован!");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            Auth savedUser = authRepository.save(user);
            savedUser.setPassword(null);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при регистрации!");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Auth user, HttpServletResponse response) {
        try {
            Optional<Auth> existingUser = authRepository.findByEmail(user.getEmail());
            if (existingUser.isPresent()
                    && passwordEncoder.matches(user.getPassword(), existingUser.get().getPassword())) {
                String token = generateToken(existingUser.get().getEmail());
                setJwtCookie(response, token);

                Auth userWithoutPassword = existingUser.get();
                userWithoutPassword.setPassword(null);
                return ResponseEntity.ok(userWithoutPassword);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка при входе!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при входе!");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        String token = getTokenFromCookies(request);
        if (token == null) {
            return ResponseEntity.ok(null);
        }
        String email = validateToken(token);
        Optional<Auth> user = authRepository.findByEmail(email);
        if (user.isEmpty())
            return ResponseEntity.ok(null);

        Auth currentUser = user.get();
        currentUser.setPassword(null);
        return ResponseEntity.ok(currentUser);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok("Logged out");
    }

    @GetMapping("/users")
    public ResponseEntity<?> users() {
        try {
            List<Auth> allUsers = authRepository.findAll();
            allUsers.forEach(u -> u.setPassword(null));
            return ResponseEntity.ok(allUsers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при получении списка пользователей");
        }
    }

    private String generateToken(String email) {
        return JWT.create()
                .withSubject(email)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .sign(Algorithm.HMAC256(jwtSecret));
    }

    private String validateToken(String token) {
        return JWT.require(Algorithm.HMAC256(jwtSecret))
                .build()
                .verify(token)
                .getSubject();
    }

    private void setJwtCookie(HttpServletResponse response, String token) {
        String cookie = "jwt=" + token +
                "; HttpOnly" +
                "; Max-Age=" + (7 * 24 * 60 * 60) +
                "; Path=/" +
                "; SameSite=Lax";
        response.addHeader("Set-Cookie", cookie);
    }

    private String getTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null)
            return null;
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("jwt")) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
