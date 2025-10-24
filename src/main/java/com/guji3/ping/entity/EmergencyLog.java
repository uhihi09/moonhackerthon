package com.guji3.ping.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_log", indexes = {
        @Index(name = "idx_user_created", columnList = "user_id, created_at"),
        @Index(name = "idx_danger_level", columnList = "danger_level")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 위치 정보
    @Column(precision = 10, scale = 8)
    private BigDecimal latitude; // 위도

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude; // 경도

    @Column(name = "location_address", length = 500)
    private String locationAddress; // "서울시 강남구 테헤란로 123"

    // AI 분석 정보
    @Column(name = "audio_text", columnDefinition = "TEXT")
    private String audioText; // Whisper API 결과: "도와주세요! 살려주세요!"

    @Column(name = "situation_analysis", columnDefinition = "TEXT")
    private String situationAnalysis; // GPT 분석: "납치 또는 강도 위험 가능성 높음"

    @Column(name = "danger_level", length = 20)
    @Enumerated(EnumType.STRING)
    private DangerLevel dangerLevel; // HIGH, MEDIUM, LOW

    // 전송 정보
    @Column(name = "sent_contacts", columnDefinition = "TEXT")
    private String sentContacts; // JSON: [{"name":"엄마","phone":"010-1111-2222"}]

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "notification_success", nullable = false)
    @Builder.Default
    private Boolean notificationSuccess = false;

    // 기타
    @Column(name = "device_serial", length = 100)
    private String deviceSerial;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Enum 정의
    public enum DangerLevel {
        HIGH("매우 위급"),
        MEDIUM("주의 필요"),
        LOW("경미한 상황");

        private final String description;

        DangerLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}