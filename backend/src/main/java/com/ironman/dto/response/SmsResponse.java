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
public class SmsResponse {

    private Long id;
    private String phoneNumber;
    private String message;
    private String smsType;
    private String twilioSid;
    private String status;
    private LocalDateTime sentAt;
}