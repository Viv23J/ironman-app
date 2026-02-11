package com.ironman.service;

import com.ironman.config.FileStorageConfig;
import com.ironman.dto.response.FileUploadResponse;
import com.ironman.exception.BadRequestException;
import com.ironman.model.FileMetadata;
import com.ironman.model.User;
import com.ironman.repository.FileMetadataRepository;
import com.ironman.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final FileStorageConfig fileStorageConfig;
    private final FileMetadataRepository fileMetadataRepository;
    private final UserRepository userRepository;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * Upload profile image
     */
    @Transactional
    public FileUploadResponse uploadProfileImage(Long userId, MultipartFile file) {
        log.info("Uploading profile image for user: {}", userId);

        validateImageFile(file);

        String subDir = "profiles";
        String storedFileName = storeFile(file, subDir);

        // Save metadata
        User user = userRepository.findById(userId).orElse(null);
        FileMetadata metadata = saveFileMetadata(file, storedFileName, subDir, "PROFILE_IMAGE", user);

        // Update user profile image
        if (user != null) {
            user.setProfileImageUrl("/api/v1/files/" + storedFileName);
            userRepository.save(user);
        }

        String fileUrl = "/api/v1/files/" + storedFileName;

        log.info("Profile image uploaded successfully");

        return FileUploadResponse.builder()
                .id(metadata.getId())
                .fileName(metadata.getOriginalFileName())
                .fileUrl(fileUrl)
                .fileType("PROFILE_IMAGE")
                .fileSize(metadata.getFileSize())
                .message("Profile image uploaded successfully")
                .build();
    }

    /**
     * Upload document (partner license, aadhar, etc.)
     */
    @Transactional
    public FileUploadResponse uploadDocument(Long userId, MultipartFile file, String documentType) {
        log.info("Uploading document for user: {}", userId);

        validateDocumentFile(file);

        String subDir = "documents";
        String storedFileName = storeFile(file, subDir);

        User user = userRepository.findById(userId).orElse(null);
        FileMetadata metadata = saveFileMetadata(file, storedFileName, subDir, documentType, user);

        String fileUrl = "/api/v1/files/" + storedFileName;

        log.info("Document uploaded successfully");

        return FileUploadResponse.builder()
                .id(metadata.getId())
                .fileName(metadata.getOriginalFileName())
                .fileUrl(fileUrl)
                .fileType(documentType)
                .fileSize(metadata.getFileSize())
                .message("Document uploaded successfully")
                .build();
    }

    /**
     * Load file as resource
     */
    public Resource loadFileAsResource(String fileName) {
        try {
            // fileName already includes subdirectory (e.g., "profiles/abc.jpg")
            Path filePath = Paths.get(fileStorageConfig.getUploadDir())
                    .resolve(fileName)
                    .normalize();

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new BadRequestException("File not found or not readable: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new BadRequestException("Invalid file path: " + fileName);
        }
    }

    /**
     * Delete file
     */
    @Transactional
    public void deleteFile(Long fileId, Long userId) {
        log.info("Deleting file: {}", fileId);

        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new BadRequestException("File not found"));

        // Verify ownership
        if (metadata.getUploadedBy() != null && !metadata.getUploadedBy().getId().equals(userId)) {
            throw new BadRequestException("You don't have permission to delete this file");
        }

        // Delete physical file
        try {
            Path filePath = Paths.get(fileStorageConfig.getUploadDir()).resolve(metadata.getStoredFileName()).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            log.error("Error deleting physical file", ex);
        }

        // Delete metadata
        fileMetadataRepository.delete(metadata);

        log.info("File deleted successfully");
    }

    /**
     * Get user's uploaded files
     */
    public List<FileMetadata> getUserFiles(Long userId) {
        return fileMetadataRepository.findByUploadedById(userId);
    }

    // =============================================
    // PRIVATE HELPER METHODS
    // =============================================

    /**
     * Store file on disk
     */
    private String storeFile(MultipartFile file, String subDir) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check for invalid characters
            if (originalFileName.contains("..")) {
                throw new BadRequestException("Filename contains invalid path sequence: " + originalFileName);
            }

            // Generate unique filename
            String fileExtension = getFileExtension(originalFileName);
            String storedFileName = UUID.randomUUID().toString() + "." + fileExtension;

            log.info("UPLOAD DIR: {}", fileStorageConfig.getUploadDir());
            log.info("SUB DIR: {}", subDir);
            log.info("TARGET LOCATION: {}", Paths.get(fileStorageConfig.getUploadDir() + subDir).resolve(storedFileName).toAbsolutePath());

            Path targetLocation = Paths.get(fileStorageConfig.getUploadDir() + subDir).resolve(storedFileName);
            Files.createDirectories(targetLocation.getParent());

            // Copy file
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return subDir + "/" + storedFileName;

        } catch (IOException ex) {
            throw new BadRequestException("Could not store file. Please try again!");
        }
    }

    /**
     * Save file metadata to database
     */
    private FileMetadata saveFileMetadata(MultipartFile file, String storedFileName,
                                          String subDir, String fileType, User user) {
        FileMetadata metadata = new FileMetadata();
        metadata.setOriginalFileName(file.getOriginalFilename());
        metadata.setStoredFileName(storedFileName);
        metadata.setFilePath(fileStorageConfig.getUploadDir() + storedFileName);
        metadata.setFileType(fileType);
        metadata.setMimeType(file.getContentType());
        metadata.setFileSize(file.getSize());
        metadata.setUploadedBy(user);

        return fileMetadataRepository.save(metadata);
    }

    /**
     * Validate image file
     */
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum limit of 5MB");
        }

        String fileName = file.getOriginalFilename();
        String extension = getFileExtension(fileName).toLowerCase();

        List<String> allowedTypes = Arrays.asList(fileStorageConfig.getAllowedImageTypes().split(","));

        if (!allowedTypes.contains(extension)) {
            throw new BadRequestException("Invalid file type. Allowed types: " +
                    String.join(", ", allowedTypes));
        }
    }

    /**
     * Validate document file
     */
    private void validateDocumentFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum limit of 5MB");
        }

        String fileName = file.getOriginalFilename();
        String extension = getFileExtension(fileName).toLowerCase();

        List<String> allowedTypes = Arrays.asList(fileStorageConfig.getAllowedDocumentTypes().split(","));

        if (!allowedTypes.contains(extension)) {
            throw new BadRequestException("Invalid file type. Allowed types: " +
                    String.join(", ", allowedTypes));
        }
    }

    /**
     * Get file extension
     */
    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }

        return fileName.substring(lastDotIndex + 1);
    }
}