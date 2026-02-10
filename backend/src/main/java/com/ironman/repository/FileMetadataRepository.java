package com.ironman.repository;

import com.ironman.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    List<FileMetadata> findByUploadedById(Long userId);

    Optional<FileMetadata> findByStoredFileName(String storedFileName);

    List<FileMetadata> findByFileType(String fileType);
}