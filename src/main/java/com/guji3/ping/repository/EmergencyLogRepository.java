package com.guji3.ping.repository;

import com.guji3.ping.entity.EmergencyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmergencyLogRepository extends JpaRepository<EmergencyLog, Long> {

    // 사용자의 긴급 신호 이력 (최신순)
    List<EmergencyLog> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    // 특정 기간 내 긴급 신호
    List<EmergencyLog> findByUser_UserIdAndCreatedAtBetween(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // 최근 24시간 내 긴급 신호 개수
    @Query("SELECT COUNT(e) FROM EmergencyLog e WHERE e.user.userId = :userId " +
            "AND e.createdAt >= :since")
    long countRecentEmergencies(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    // 기기 시리얼로 최근 로그 조회
    List<EmergencyLog> findTop10ByDeviceSerialOrderByCreatedAtDesc(String deviceSerial);
}