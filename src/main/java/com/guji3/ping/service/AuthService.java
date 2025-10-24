package com.guji3.ping.service;

import com.guji3.ping.entity.User;
import com.guji3.ping.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 로그인 (JWT 토큰 발급)
     */
    public Map<String, Object> login(String email, String password) {
        // 사용자 조회
        User user = userService.findByEmail(email);

        // 비밀번호 검증
        if (!userService.verifyPassword(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        // JWT 토큰 생성
        String token = jwtUtil.generateToken(user.getEmail(), user.getUserId());

        log.info("✅ 로그인 성공: {} (ID: {})", user.getEmail(), user.getUserId());

        // 응답 데이터
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", user.getUserId());
        response.put("email", user.getEmail());
        response.put("name", user.getName());
        response.put("phone", user.getPhone());
        response.put("deviceSerial", user.getDeviceSerial());

        return response;
    }

    /**
     * 토큰 검증
     */
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    /**
     * 토큰에서 이메일 추출
     */
    public String getEmailFromToken(String token) {
        return jwtUtil.getEmailFromToken(token);
    }
}