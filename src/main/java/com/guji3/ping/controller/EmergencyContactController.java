package com.guji3.ping.controller;

import com.guji3.ping.dto.EmergencyContactDto;
import com.guji3.ping.entity.EmergencyContact;
import com.guji3.ping.entity.User;
import com.guji3.ping.service.EmergencyContactService;
import com.guji3.ping.service.UserService;
import jakarta.validation.Valid;
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
@RequestMapping("/contacts")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class EmergencyContactController {

    private final EmergencyContactService contactService;
    private final UserService userService;

    /**
     * 긴급 연락처 목록 조회
     * GET /api/contacts
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getContacts(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);

        List<EmergencyContact> contacts = contactService.getActiveContacts(user.getUserId());

        List<Map<String, Object>> contactList = contacts.stream()
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("contactId", c.getContactId());
                    map.put("contactName", c.getContactName());
                    map.put("contactPhone", c.getContactPhone());
                    map.put("contactEmail", c.getContactEmail());
                    map.put("priority", c.getPriority());
                    map.put("isActive", c.getIsActive());
                    map.put("createdAt", c.getCreatedAt());
                    return map;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("contacts", contactList);
        response.put("totalCount", contactList.size());

        return ResponseEntity.ok(response);
    }

    /**
     * 긴급 연락처 추가
     * POST /api/contacts
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addContact(
            Authentication authentication,
            @Valid @RequestBody EmergencyContactDto dto) {

        String email = authentication.getName();
        User user = userService.findByEmail(email);

        log.info("📞 긴급 연락처 추가: {} - {}", user.getName(), dto.getContactName());

        EmergencyContact contact = contactService.addContact(user.getUserId(), dto);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "긴급 연락처가 추가되었습니다");
        response.put("contactId", contact.getContactId());
        response.put("contactName", contact.getContactName());

        return ResponseEntity.ok(response);
    }

    /**
     * 긴급 연락처 수정
     * PUT /api/contacts/{contactId}
     */
    @PutMapping("/{contactId}")
    public ResponseEntity<Map<String, Object>> updateContact(
            @PathVariable Long contactId,
            @Valid @RequestBody EmergencyContactDto dto) {

        log.info("✏️ 긴급 연락처 수정: ID {}", contactId);

        EmergencyContact contact = contactService.updateContact(contactId, dto);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "긴급 연락처가 수정되었습니다");
        response.put("contactId", contact.getContactId());

        return ResponseEntity.ok(response);
    }

    /**
     * 긴급 연락처 삭제
     * DELETE /api/contacts/{contactId}
     */
    @DeleteMapping("/{contactId}")
    public ResponseEntity<Map<String, Object>> deleteContact(@PathVariable Long contactId) {
        log.info("🗑️ 긴급 연락처 삭제: ID {}", contactId);

        contactService.deleteContact(contactId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "긴급 연락처가 삭제되었습니다");

        return ResponseEntity.ok(response);
    }

    /**
     * 우선순위 재정렬
     * PUT /api/contacts/reorder
     */
    @PutMapping("/reorder")
    public ResponseEntity<Map<String, Object>> reorderContacts(
            Authentication authentication,
            @RequestBody Map<String, List<Long>> request) {

        String email = authentication.getName();
        User user = userService.findByEmail(email);

        List<Long> contactIds = request.get("contactIds");

        log.info("🔄 우선순위 재정렬: 사용자 {}", user.getName());

        contactService.reorderPriorities(user.getUserId(), contactIds);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "우선순위가 재정렬되었습니다");

        return ResponseEntity.ok(response);
    }
}