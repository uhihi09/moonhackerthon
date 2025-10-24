package com.guji3.ping.dto;

import com.guji3.ping.entity.EmergencyLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyResponseDto {

    private Long logId;
    private String userName;
    private String userPhone;

    // 위치 정보
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String locationAddress;

    // AI 분석 결과
    private String audioText;
    private String situationAnalysis;
    private EmergencyLog.DangerLevel dangerLevel;

    // 알림 정보
    private List<ContactInfo> sentTo;
    private Boolean notificationSuccess;

    private LocalDateTime createdAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ContactInfo {
        private String name;
        private String phone;
    }
}