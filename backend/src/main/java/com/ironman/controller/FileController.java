package com.ironman.controller;

import com.ironman.dto.response.ApiResponse;
import com.ironman.dto.response.FileUploadResponse;
import com.ironman.security.UserDetailsImpl;
import com.ironman.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileStorageService fileStorageService;

    /**
     * Upload profile image
     */
    @PostMapping("/upload/profile")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadProfileImage(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestParam("file") MultipartFile file) {

        log.info("Uploading profile image for user: {}", currentUser.getId());
        FileUploadResponse response = fileStorageService.uploadProfileImage(currentUser.getId(), file);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Profile image uploaded successfully", response));
    }

    /**
     * Upload document (license, aadhar, etc.)
     */
    @PostMapping("/upload/document")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadDocument(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType) {

        log.info("Uploading document for user: {}", currentUser.getId());
        FileUploadResponse response = fileStorageService.uploadDocument(
                currentUser.getId(), file, documentType);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document uploaded successfully", response));
    }

    /**
     * Download/View file
     */
    @GetMapping("/{subDir}/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String subDir,
            @PathVariable String fileName,
            HttpServletRequest request) {

        log.info("Loading file: {}/{}", subDir, fileName);

        // Construct full path with subdirectory
        String fullPath = subDir + "/" + fileName;

        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fullPath);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.info("Could not determine file type.");
        }

        // Fallback to default content type
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * Delete file
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResponse<String>> deleteFile(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable Long fileId) {

        log.info("Deleting file: {}", fileId);
        fileStorageService.deleteFile(fileId, currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.success("File deleted successfully", null));
    }
}