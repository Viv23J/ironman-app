package com.ironman.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class FileStorageConfig {

    @Value("${file.upload.dir}")
    private String uploadDir;

    @Value("${file.upload.max-file-size}")
    private String maxFileSize;

    @Value("${file.upload.allowed-image-types}")
    private String allowedImageTypes;

    @Value("${file.upload.allowed-document-types}")
    private String allowedDocumentTypes;
}