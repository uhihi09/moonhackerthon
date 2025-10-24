package com.guji3.ping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyContactDto {

    private Long contactId; // 수정 시 필요

    @NotBlank(message = "연락처 이름은 필수입니다")
    private String contactName;

    @NotBlank(message = "전화번호는 필수입니다")
    private String contactPhone;

    private String contactEmail;

    @NotNull(message = "우선순위는 필수입니다")
    private Integer priority;

    @Builder.Default
    private Boolean isActive = true;
}