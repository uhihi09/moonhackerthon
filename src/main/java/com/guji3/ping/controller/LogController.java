package com.guji3.ping.controller;

import com.guji3.ping.entity.EmergencyLog;
import com.guji3.ping.entity.User;
import com.guji3.ping.service.EmergencyService;
import com.guji3.ping.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class LogController {

    private final EmergencyService emergencyService;
    private final UserService userService;

    /**
     * 내 긴급 신호 이력 조회
     * GET /api/logs
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMyEmergencyLogs(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);

        List<EmergencyLog> logs = emergencyService.getUserEmergencyHistory(user.getUserId());

        List<Map<String, Object>> logList = logs.stream()
                .map(log -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("logId", log.getLogId());
                    map.put("latitude", log.getLatitude());
                    map.put("longitude", log.getLongitude());
                    map.put("locationAddress", log.getLocationAddress());
                    map.put("audioText", log.getAudioText());
                    map.put("situationAnalysis", log.getSituationAnalysis());
                    map.put("dangerLevel", log.getDangerLevel());
                    map.put("notificationSuccess", log.getNotificationSuccess());
                    map.put("createdAt", log.getCreatedAt());
                    return map;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("logs", logList);
        response.put("totalCount", logList.size());

        return ResponseEntity.ok(response);
    }

    /**
     * 최근 24시간 긴급 신호 개수
     * GET /api/logs/recent-count
     */
    @GetMapping("/recent-count")
    public ResponseEntity<Map<String, Object>> getRecentCount(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);

        long count = emergencyService.countRecentEmergencies(user.getUserId());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", count);
        response.put("period", "최근 24시간");

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 긴급 로그 상세 조회
     * GET /api/logs/{logId}
     */
    @GetMapping("/{logId}")
    public ResponseEntity<Map<String, Object>> getLogDetail(@PathVariable Long logId) {
        // 상세 조회 로직 구현

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("logId", logId);

        return ResponseEntity.ok(response);
    }
}