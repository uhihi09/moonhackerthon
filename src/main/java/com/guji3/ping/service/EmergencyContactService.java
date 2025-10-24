package com.guji3.ping.service;

import com.guji3.ping.dto.EmergencyContactDto;
import com.guji3.ping.entity.EmergencyContact;
import com.guji3.ping.entity.User;
import com.guji3.ping.repository.EmergencyContactRepository;
import com.guji3.ping.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EmergencyContactService {

    private final EmergencyContactRepository contactRepository;
    private final UserRepository userRepository;

    /**
     * 긴급 연락처 추가
     */
    @Transactional
    public EmergencyContact addContact(Long userId, EmergencyContactDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다"));

        // 최대 5개 제한 (해커톤용 제약)
        long count = contactRepository.countByUser_UserId(userId);
        if (count >= 5) {
            throw new IllegalArgumentException("긴급 연락처는 최대 5개까지 등록 가능합니다");
        }

        EmergencyContact contact = EmergencyContact.builder()
                .user(user)
                .contactName(dto.getContactName())
                .contactPhone(dto.getContactPhone())
                .contactEmail(dto.getContactEmail())
                .priority(dto.getPriority())
                .isActive(dto.getIsActive())
                .build();

        EmergencyContact saved = contactRepository.save(contact);
        log.info("📞 긴급 연락처 추가: {} - {} ({})", user.getName(),
                saved.getContactName(), saved.getContactPhone());

        return saved;
    }

    /**
     * 사용자의 모든 긴급 연락처 조회 (우선순위 순)
     */
    public List<EmergencyContact> getActiveContacts(Long userId) {
        return contactRepository.findByUser_UserIdAndIsActiveTrueOrderByPriorityAsc(userId);
    }

    /**
     * 긴급 연락처 수정
     */
    @Transactional
    public EmergencyContact updateContact(Long contactId, EmergencyContactDto dto) {
        EmergencyContact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 연락처입니다"));

        contact.setContactName(dto.getContactName());
        contact.setContactPhone(dto.getContactPhone());
        contact.setContactEmail(dto.getContactEmail());
        contact.setPriority(dto.getPriority());
        contact.setIsActive(dto.getIsActive());

        log.info("✏️ 긴급 연락처 수정: ID {}", contactId);

        return contact;
    }

    /**
     * 긴급 연락처 삭제
     */
    @Transactional
    public void deleteContact(Long contactId) {
        if (!contactRepository.existsById(contactId)) {
            throw new IllegalArgumentException("존재하지 않는 연락처입니다");
        }

        contactRepository.deleteById(contactId);
        log.info("🗑️ 긴급 연락처 삭제: ID {}", contactId);
    }

    /**
     * 우선순위 재정렬
     */
    @Transactional
    public void reorderPriorities(Long userId, List<Long> contactIds) {
        for (int i = 0; i < contactIds.size(); i++) {
            Long contactId = contactIds.get(i);
            EmergencyContact contact = contactRepository.findById(contactId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 연락처입니다"));

            // 다른 사용자의 연락처는 수정 불가
            if (!contact.getUser().getUserId().equals(userId)) {
                throw new IllegalArgumentException("권한이 없습니다");
            }

            contact.setPriority(i + 1);
        }

        log.info("🔄 우선순위 재정렬 완료: 사용자 ID {}", userId);
    }
}