package com.ironman.controller;

import com.ironman.dto.request.ProfileUpdateRequest;
import com.ironman.dto.response.ApiResponse;
import com.ironman.dto.response.UserProfileResponse;
import com.ironman.security.UserDetailsImpl;
import com.ironman.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    // Get current user profile
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        log.info("Fetching profile for user: {}", currentUser.getId());
        UserProfileResponse profile = userService.getProfile(currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Profile fetched successfully", profile));
    }

    // Update current user profile
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @RequestBody ProfileUpdateRequest request) {

        log.info("Updating profile for user: {}", currentUser.getId());
        UserProfileResponse profile = userService.updateProfile(currentUser.getId(), request);

        return ResponseEntity.ok(
                ApiResponse.success("Profile updated successfully", profile));
    }

    // Upload profile image
    @PostMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<UserProfileResponse>> uploadProfileImage(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestParam("image") MultipartFile image) {

        log.info("Uploading profile image for user: {}", currentUser.getId());

        // For now, save a placeholder URL
        // We'll implement actual file upload (S3/Cloudinary) later
        String imageUrl = "https://placeholder.com/profile-" + currentUser.getId() + ".jpg";

        UserProfileResponse profile = userService.updateProfileImage(currentUser.getId(), imageUrl);

        return ResponseEntity.ok(
                ApiResponse.success("Profile image uploaded successfully", profile));
    }
}