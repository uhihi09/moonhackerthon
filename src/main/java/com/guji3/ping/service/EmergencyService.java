package com.guji3.ping.service;

import com.google.gson.Gson;
import com.guji3.ping.dto.EmergencyRequestDto;
import com.guji3.ping.dto.EmergencyResponseDto;
import com.guji3.ping.entity.EmergencyContact;
import com.guji3.ping.entity.EmergencyLog;
import com.guji3.ping.entity.User;
import com.guji3.ping.repository.EmergencyLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EmergencyService {

    private final UserService userService;
    private final EmergencyContactService contactService;
    private final AiAnalysisService aiAnalysisService;
    private final LocationService locationService;
    private final NotificationService notificationService;
    private final EmergencyLogRepository logRepository;
    private final Gson gson = new Gson();

    /**
     * 긴급 신호 처리 (핵심 메서드!)
     */
    @Transactional
    public EmergencyResponseDto processEmergency(String deviceSerial,
                                                 EmergencyRequestDto request,
                                                 MultipartFile audioFile) throws Exception {

        log.info("🚨 긴급 신호 수신: 기기 {}", deviceSerial);

        // 1단계: 사용자 확인
        User user = userService.findByDeviceSerial(deviceSerial);
        log.info("👤 사용자 확인: {} ({})", user.getName(), user.getEmail());

        // 2단계: 긴급 연락처 조회
        List<EmergencyContact> contacts = contactService.getActiveContacts(user.getUserId());
        if (contacts.isEmpty()) {
            throw new IllegalStateException("등록된 긴급 연락처가 없습니다");
        }
        log.info("📞 긴급 연락처: {}명", contacts.size());

        // 3단계: AI 음성 분석
        Map<String, String> aiAnalysis = aiAnalysisService.fullAnalysis(audioFile);
        String audioText = aiAnalysis.get("audioText");
        String situation = aiAnalysis.get("situation");
        String dangerLevelStr = aiAnalysis.get("dangerLevel");
        String analysis = aiAnalysis.get("analysis");

        EmergencyLog.DangerLevel dangerLevel;
        try {
            dangerLevel = EmergencyLog.DangerLevel.valueOf(dangerLevelStr);
        } catch (Exception e) {
            dangerLevel = EmergencyLog.DangerLevel.MEDIUM;
        }

        log.info("🤖 AI 분석 완료: 상황={}, 위험도={}", situation, dangerLevel);

        // 4단계: 위치 정보 처리
        String address = locationService.getAddressFromCoordinates(
                request.getLatitude(),
                request.getLongitude()
        );
        log.info("📍 위치 확인: {}", address);

        // 5단계: SMS 발송
        Map<String, Boolean> smsResults = notificationService.sendBulkEmergencySms(
                contacts,
                user.getName(),
                request.getLatitude(),
                request.getLongitude(),
                address,
                situation
        );

        boolean allSuccess = smsResults.values().stream().allMatch(v -> v);

        // 6단계: DB 저장
        List<Map<String, String>> sentContactsInfo = contacts.stream()
                .map(c -> Map.of("name", c.getContactName(), "phone", c.getContactPhone()))
                .collect(Collectors.toList());

        EmergencyLog log = EmergencyLog.builder()
                .user(user)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .locationAddress(address)
                .audioText(audioText)
                .situationAnalysis(analysis)
                .dangerLevel(dangerLevel)
                .sentContacts(gson.toJson(sentContactsInfo))
                .sentAt(LocalDateTime.now())
                .notificationSuccess(allSuccess)
                .deviceSerial(deviceSerial)
                .build();

        EmergencyLog savedLog = logRepository.save(log);
        this.log.info("💾 긴급 로그 저장 완료: ID {}", savedLog.getLogId());

        // 7단계: 응답 생성
        return EmergencyResponseDto.builder()
                .logId(savedLog.getLogId())
                .userName(user.getName())
                .userPhone(user.getPhone())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .locationAddress(address)
                .audioText(audioText)
                .situationAnalysis(analysis)
                .dangerLevel(dangerLevel)
                .sentTo(sentContactsInfo.stream()
                        .map(c -> new EmergencyResponseDto.ContactInfo(c.get("name"), c.get("phone")))
                        .collect(Collectors.toList()))
                .notificationSuccess(allSuccess)
                .createdAt(savedLog.getCreatedAt())
                .build();
    }

    /**
     * 사용자의 긴급 이력 조회
     */
    public List<EmergencyLog> getUserEmergencyHistory(Long userId) {
        return logRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 최근 24시간 긴급 신호 개수
     */
    public long countRecentEmergencies(Long userId) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return logRepository.countRecentEmergencies(userId, since);
    }
}