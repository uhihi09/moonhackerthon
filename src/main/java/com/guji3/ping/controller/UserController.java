package com.guji3.ping.controller;

import com.guji3.ping.entity.User;
import com.guji3.ping.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    /**
     * 내 정보 조회
     * GET /api/user/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getUserId());
        response.put("email", user.getEmail());
        response.put("name", user.getName());
        response.put("phone", user.getPhone());
        response.put("deviceSerial", user.getDeviceSerial());
        response.put("createdAt", user.getCreatedAt());

        return ResponseEntity.ok(response);
    }

    /**
     * 아두이노 기기 등록/변경
     * PUT /api/user/device
     */
    @PutMapping("/device")
    public ResponseEntity<Map<String, Object>> registerDevice(
            Authentication authentication,
            @RequestBody Map<String, String> request) {

        String email = authentication.getName();
        String deviceSerial = request.get("deviceSerial");

        log.info("🔧 기기 등록 요청: {} - {}", email, deviceSerial);

        User user = userService.findByEmail(email);
        User updatedUser = userService.updateDeviceSerial(user.getUserId(), deviceSerial);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "기기가 등록되었습니다");
        response.put("deviceSerial", updatedUser.getDeviceSerial());

        return ResponseEntity.ok(response);
    }

    /**
     * 전화번호 수정
     * PUT /api/user/phone
     */
    @PutMapping("/phone")
    public ResponseEntity<Map<String, Object>> updatePhone(
            Authentication authentication,
            @RequestBody Map<String, String> request) {

        String email = authentication.getName();
        String newPhone = request.get("phone");

        User user = userService.findByEmail(email);
        user.setPhone(newPhone);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "전화번호가 변경되었습니다");
        response.put("phone", newPhone);

        return ResponseEntity.ok(response);
    }
}