package com.guji3.ping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyRequestDto {

    private String deviceSerial; // 아두이노 기기 시리얼

    private BigDecimal latitude;  // GPS 위도
    private BigDecimal longitude; // GPS 경도

    private String audioBase64; // Base64 인코딩된 음성 데이터

    // 또는
    private String audioUrl; // 업로드된 음성 파일 URL
}