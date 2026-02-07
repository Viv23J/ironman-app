package com.ironman.controller;

import com.ironman.dto.request.FcmTokenRequest;
import com.ironman.dto.response.ApiResponse;
import com.ironman.dto.response.NotificationResponse;
import com.ironman.security.UserDetailsImpl;
import com.ironman.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Save FCM token
     */
    @PostMapping("/fcm-token")
    public ResponseEntity<ApiResponse<String>> saveFcmToken(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @RequestBody FcmTokenRequest request) {

        log.info("Saving FCM token for user: {}", currentUser.getId());
        notificationService.saveFcmToken(currentUser.getId(), request);

        return ResponseEntity.ok(
                ApiResponse.success("FCM token saved successfully", null));
    }

    /**
     * Get all my notifications
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        log.info("Fetching notifications for user: {}", currentUser.getId());
        List<NotificationResponse> notifications = notificationService.getUserNotifications(currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Notifications fetched successfully", notifications));
    }

    /**
     * Get unread notifications
     */
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        log.info("Fetching unread notifications for user: {}", currentUser.getId());
        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Unread notifications fetched successfully", notifications));
    }

    /**
     * Get unread count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        log.info("Fetching unread count for user: {}", currentUser.getId());
        long count = notificationService.getUnreadCount(currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Unread count fetched successfully", Map.of("count", count)));
    }

    /**
     * Mark notification as read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<String>> markAsRead(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable Long id) {

        log.info("Marking notification {} as read", id);
        notificationService.markAsRead(id, currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Notification marked as read", null));
    }

    /**
     * Mark all notifications as read
     */
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<String>> markAllAsRead(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        log.info("Marking all notifications as read for user: {}", currentUser.getId());
        notificationService.markAllAsRead(currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.success("All notifications marked as read", null));
    }
}