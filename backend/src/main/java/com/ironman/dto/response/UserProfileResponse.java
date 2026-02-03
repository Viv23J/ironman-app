package com.ironman.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String phone;
    private String email;
    private String fullName;
    private String role;
    private String profileImageUrl;
    private Boolean isActive;
    private Boolean isVerified;
    private LocalDateTime createdAt;
}