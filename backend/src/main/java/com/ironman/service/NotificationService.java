package com.ironman.service;

import com.ironman.dto.request.FcmTokenRequest;
import com.ironman.dto.response.NotificationResponse;
import com.ironman.exception.ResourceNotFoundException;
import com.ironman.model.Notification;
import com.ironman.model.NotificationType;
import com.ironman.model.User;
import com.ironman.repository.NotificationRepository;
import com.ironman.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    // TODO: Add FirebaseMessaging when we configure Firebase

    /**
     * Save FCM token for user
     */
    @Transactional
    public void saveFcmToken(Long userId, FcmTokenRequest request) {
        log.info("Saving FCM token for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setFcmToken(request.getFcmToken());
        userRepository.save(user);

        log.info("FCM token saved successfully");
    }

    /**
     * Create and send notification
     */
    @Transactional
    public void createNotification(Long userId, NotificationType type, String title,
                                   String message, Long orderId) {
        log.info("Creating notification for user {}: {}", userId, type);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Save notification in database
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setOrderId(orderId);
        notification.setIsRead(false);
        notification.setFcmSent(false);

        notificationRepository.save(notification);

        // TODO: Send FCM push notification
        // if (user.getFcmToken() != null) {
        //     sendFcmNotification(user.getFcmToken(), title, message);
        // }

        log.info("Notification created successfully");
    }

    /**
     * Get all notifications for user
     */
    public List<NotificationResponse> getUserNotifications(Long userId) {
        log.info("Fetching notifications for user: {}", userId);

        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return notifications.stream()
                .map(this::mapToNotificationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get unread notifications
     */
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        log.info("Fetching unread notifications for user: {}", userId);

        List<Notification> notifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);

        return notifications.stream()
                .map(this::mapToNotificationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get unread count
     */
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        log.info("Marking notification {} as read", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        // Verify notification belongs to user
        if (!notification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    /**
     * Mark all as read
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for user: {}", userId);

        List<Notification> unread = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);

        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);

        log.info("Marked {} notifications as read", unread.size());
    }

    // =============================================
    // HELPER METHODS FOR SPECIFIC NOTIFICATIONS
    // =============================================

    public void notifyOrderCreated(Long userId, Long orderId, String orderNumber) {
        createNotification(
                userId,
                NotificationType.ORDER_CREATED,
                "Order Placed Successfully",
                "Your order " + orderNumber + " has been placed successfully.",
                orderId
        );
    }

    public void notifyPaymentSuccess(Long userId, Long orderId, String orderNumber) {
        createNotification(
                userId,
                NotificationType.PAYMENT_SUCCESS,
                "Payment Successful",
                "Payment for order " + orderNumber + " was successful.",
                orderId
        );
    }

    public void notifyPickupAssigned(Long userId, Long orderId, String orderNumber, String partnerName) {
        createNotification(
                userId,
                NotificationType.PICKUP_ASSIGNED,
                "Pickup Scheduled",
                partnerName + " will pickup your order " + orderNumber + " soon.",
                orderId
        );
    }

    public void notifyDeliveryAssigned(Long userId, Long orderId, String orderNumber, String partnerName) {
        createNotification(
                userId,
                NotificationType.DELIVERY_ASSIGNED,
                "Out for Delivery",
                partnerName + " is delivering your order " + orderNumber + ".",
                orderId
        );
    }

    public void notifyDelivered(Long userId, Long orderId, String orderNumber) {
        createNotification(
                userId,
                NotificationType.DELIVERED,
                "Order Delivered",
                "Your order " + orderNumber + " has been delivered successfully.",
                orderId
        );
    }

    // =============================================
    // PRIVATE HELPERS
    // =============================================

    private NotificationResponse mapToNotificationResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType().name())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .orderId(notification.getOrderId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}