package com.ironman.service;

import com.ironman.dto.request.ProfileUpdateRequest;
import com.ironman.dto.response.UserProfileResponse;
import com.ironman.exception.BadRequestException;
import com.ironman.exception.ResourceNotFoundException;
import com.ironman.model.User;
import com.ironman.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    public User getUserByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with phone: " + phone));
    }

    public UserProfileResponse getProfile(Long userId) {
        log.info("Fetching profile for user: {}", userId);
        User user = getUserById(userId);
        return mapToProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        log.info("Updating profile for user: {}", userId);

        User user = getUserById(userId);

        // Update full name if provided
        if (request.getFullName() != null && !request.getFullName().isEmpty()) {
            user.setFullName(request.getFullName());
        }

        // Update email if provided
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            // Check if email already exists
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already registered");
            }
            user.setEmail(request.getEmail());
        }

        User updatedUser = userRepository.save(user);
        log.info("Profile updated successfully for user: {}", userId);

        return mapToProfileResponse(updatedUser);
    }

    @Transactional
    public UserProfileResponse updateProfileImage(Long userId, String imageUrl) {
        log.info("Updating profile image for user: {}", userId);

        User user = getUserById(userId);
        user.setProfileImageUrl(imageUrl);
        User updatedUser = userRepository.save(user);

        log.info("Profile image updated successfully");
        return mapToProfileResponse(updatedUser);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public long getUserCount() {
        return userRepository.count();
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .profileImageUrl(user.getProfileImageUrl())
                .isActive(user.getIsActive())
                .isVerified(user.getIsVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }
}