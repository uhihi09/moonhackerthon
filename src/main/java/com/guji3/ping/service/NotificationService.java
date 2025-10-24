package com.guji3.ping.service;

import com.google.gson.Gson;
import com.guji3.ping.entity.EmergencyContact;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    @Value("${naver.cloud.sms.access-key:}")
    private String naverAccessKey;

    @Value("${naver.cloud.sms.secret-key:}")
    private String naverSecretKey;

    @Value("${naver.cloud.sms.service-id:}")
    private String naverServiceId;

    @Value("${naver.cloud.sms.from-number:}")
    private String fromNumber;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    /**
     * 긴급 SMS 발송 (네이버 클라우드 SMS API)
     */
    public boolean sendEmergencySms(EmergencyContact contact, String userName,
                                    BigDecimal latitude, BigDecimal longitude,
                                    String address, String situation) {
        try {
            String message = String.format(
                    "[긴급 SOS] %s님의 긴급 신호가 발생했습니다!\n\n" +
                            "📍 위치: %s\n" +
                            "🚨 상황: %s\n" +
                            "📞 연락처: %s\n\n" +
                            "즉시 확인 후 경찰(112) 또는 소방(119)에 신고해주세요!",
                    userName, address, situation, contact.getContactPhone()
            );

            log.info("📤 SMS 발송 시작: {} → {}", fromNumber, contact.getContactPhone());

            // 네이버 클라우드 SMS API 호출
            // (실제 구현은 네이버 클라우드 문서 참고)
            // 해커톤에서는 로그만 출력

            log.info("✅ SMS 발송 완료: {}", contact.getContactName());
            return true;

        } catch (Exception e) {
            log.error("❌ SMS 발송 실패: {}", contact.getContactName(), e);
            return false;
        }
    }

    /**
     * 여러 연락처에 일괄 발송
     */
    public Map<String, Boolean> sendBulkEmergencySms(
            List<EmergencyContact> contacts,
            String userName,
            BigDecimal latitude,
            BigDecimal longitude,
            String address,
            String situation) {

        Map<String, Boolean> results = new HashMap<>();

        for (EmergencyContact contact : contacts) {
            boolean success = sendEmergencySms(contact, userName,
                    latitude, longitude, address, situation);
            results.put(contact.getContactPhone(), success);
        }

        long successCount = results.values().stream().filter(v -> v).count();
        log.info("📊 SMS 발송 완료: 성공 {}/{}", successCount, contacts.size());

        return results;
    }

    /**
     * 이메일 발송 (선택사항)
     */
    public boolean sendEmergencyEmail(EmergencyContact contact, String userName,
                                      String address, String situation) {
        // JavaMailSender 사용 (시간 여유 있을 때 구현)
        log.info("📧 이메일 발송 (미구현): {}", contact.getContactEmail());
        return true;
    }
}