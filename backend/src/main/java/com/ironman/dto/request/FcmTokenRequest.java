package com.ironman.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FcmTokenRequest {

    @NotBlank(message = "FCM token is required")
    private String fcmToken;
}