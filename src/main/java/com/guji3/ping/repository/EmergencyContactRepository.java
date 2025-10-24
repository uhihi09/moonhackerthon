package com.guji3.ping.repository;

import com.guji3.ping.entity.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {

    // 사용자의 활성화된 연락처 목록 (우선순위 순)
    List<EmergencyContact> findByUser_UserIdAndIsActiveTrueOrderByPriorityAsc(Long userId);

    // 사용자의 연락처 개수
    long countByUser_UserId(Long userId);
}