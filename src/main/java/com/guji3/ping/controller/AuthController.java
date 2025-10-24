package com.guji3.ping.controller;

import com.guji3.ping.dto.UserRegisterDto;
import com.guji3.ping.entity.User;
import com.guji3.ping.service.AuthService;
import com.guji3.ping.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // 리액트 연동을 위해
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    /**
     * 회원가입
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserRegisterDto dto) {
        log.info("📝 회원가입 요청: {}", dto.getEmail());

        User user = userService.registerUser(dto);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "회원가입이 완료되었습니다");
        response.put("userId", user.getUserId());
        response.put("email", user.getEmail());
        response.put("name", user.getName());

        return ResponseEntity.ok(response);
    }

    /**
     * 로그인 (JWT 토큰 발급)
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        log.info("🔐 로그인 요청: {}", email);

        Map<String, Object> response = authService.login(email, password);
        response.put("success", true);
        response.put("message", "로그인 성공");

        return ResponseEntity.ok(response);
    }

    /**
     * 이메일 중복 체크
     * GET /api/auth/check-email?email=test@example.com
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        boolean exists = userService.findByEmail(email) != null;

        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("message", exists ? "이미 사용 중인 이메일입니다" : "사용 가능한 이메일입니다");

        return ResponseEntity.ok(response);
    }

    /**
     * 토큰 검증
     * GET /api/auth/verify
     */
    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyToken(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7); // "Bearer " 제거
        boolean valid = authService.validateToken(token);

        Map<String, Object> response = new HashMap<>();
        response.put("valid", valid);

        if (valid) {
            String email = authService.getEmailFromToken(token);
            response.put("email", email);
        }

        return ResponseEntity.ok(response);
    }
}