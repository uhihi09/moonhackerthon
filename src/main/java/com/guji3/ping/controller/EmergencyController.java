package com.guji3.ping.controller;

import com.guji3.ping.dto.EmergencyRequestDto;
import com.guji3.ping.dto.EmergencyResponseDto;
import com.guji3.ping.service.EmergencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/emergency")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class EmergencyController {

    private final EmergencyService emergencyService;

    /**
     * 긴급 신호 발송 (아두이노에서 호출)
     * POST /api/emergency/alert
     *
     * 요청 형식:
     * - deviceSerial: 아두이노 기기 시리얼 번호
     * - latitude: GPS 위도
     * - longitude: GPS 경도
     * - audioFile: 음성 파일 (MultipartFile)
     */
    @PostMapping(value = "/alert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EmergencyResponseDto> sendEmergencyAlert(
            @RequestParam("deviceSerial") String deviceSerial,
            @RequestParam("latitude") BigDecimal latitude,
            @RequestParam("longitude") BigDecimal longitude,
            @RequestParam("audioFile") MultipartFile audioFile) {

        log.info("🚨🚨🚨 긴급 신호 수신!");
        log.info("📱 기기: {}", deviceSerial);
        log.info("📍 좌표: {}, {}", latitude, longitude);
        log.info("🎤 음성 파일: {}, {} bytes", audioFile.getOriginalFilename(), audioFile.getSize());

        try {
            // DTO 생성
            EmergencyRequestDto request = EmergencyRequestDto.builder()
                    .deviceSerial(deviceSerial)
                    .latitude(latitude)
                    .longitude(longitude)
                    .build();

            // 긴급 신호 처리
            EmergencyResponseDto response = emergencyService.processEmergency(
                    deviceSerial, request, audioFile);

            log.info("✅ 긴급 신호 처리 완료: Log ID {}", response.getLogId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ 긴급 신호 처리 실패", e);
            throw new RuntimeException("긴급 신호 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 테스트용 간단한 긴급 신호 (음성 파일 없이)
     * POST /api/emergency/test-alert
     */
    @PostMapping("/test-alert")
    public ResponseEntity<Map<String, Object>> testEmergencyAlert(
            @RequestBody Map<String, Object> request) {

        String deviceSerial = (String) request.get("deviceSerial");

        log.info("🧪 테스트 긴급 신호: {}", deviceSerial);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "테스트 긴급 신호가 발송되었습니다");
        response.put("deviceSerial", deviceSerial);
        response.put("timestamp", java.time.LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * 긴급 신호 상태 확인
     * GET /api/emergency/status/{logId}
     */
    @GetMapping("/status/{logId}")
    public ResponseEntity<Map<String, Object>> getEmergencyStatus(@PathVariable Long logId) {
        // 로그 조회 로직 (추가 구현 필요)

        Map<String, Object> response = new HashMap<>();
        response.put("logId", logId);
        response.put("status", "처리 완료");
        response.put("notificationSent", true);

        return ResponseEntity.ok(response);
    }
}