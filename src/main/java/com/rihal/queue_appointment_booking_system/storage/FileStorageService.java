package com.rihal.queue_appointment_booking_system.storage;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${file.storage.location}")
    private String storageLocation;

    @Value("${file.max-id-image-size-bytes}")
    private long maxIdImageSizeBytes;

    @Value("${file.max-attachment-size-bytes}")
    private long maxAttachmentSizeBytes;

    private static final List<String> ALLOWED_ID_IMAGE_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/bmp"
    );

    private static final List<String> ALLOWED_ATTACHMENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/bmp",
            "application/pdf"
    );

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(storageLocation, "id-images"));
            Files.createDirectories(Paths.get(storageLocation, "attachments"));
            log.info("File storage directories have successfully been initialized at: {}", storageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize file storage directories", e);
        }
    }

    // ID Image
    /**
     * Validate and store ID Image
     * @return relative file path (stored in DB)
     */
    public String storeIdImage(MultipartFile file) {
        validateFile(file, ALLOWED_ID_IMAGE_TYPES, maxIdImageSizeBytes, "ID Image");
        return storeFile(file, "id-images");
    }

    // Appointment Attachment
    /**
     * Validate and store Appointment Attachment
     * @return file path (stored in DB)
     */
    public String storeAttachment(MultipartFile file) {
        validateFile(file, ALLOWED_ATTACHMENT_TYPES, maxAttachmentSizeBytes, "Appointment Attachment");
        return storeFile(file, "attachments");
    }

    // Load file
    /**
     * Resolve the stored file path into a readable one
     * @throws IllegalArgumentException if the file does not exist
     */
    public Path load(String relativeFilePath) {
        Path path = Paths.get(storageLocation).resolve(relativeFilePath).normalize();
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("File not found." + relativeFilePath);
        }
        return path;
    }

    // private helpers

    private String getExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) return "";
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
    }
    private String storeFile(MultipartFile file, String subDirectory) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = getExtension(originalFilename);
            String storedFilename = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
            String relativePath = subDirectory + "/" + storedFilename;

            Path targetPath = Paths.get(storageLocation, relativePath);
            Files.copy(file.getInputStream(), targetPath);
            log.info("Stored File: {}", relativePath);

            return relativePath;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file:" + e.getMessage() + e);
        }
    }
    private void validateFile(
            MultipartFile file,
            List<String> allowedTypes,
            long maxSizeBytes,
            String fileLabel) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(fileLabel + " is required and cannot be empty.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Invalid file type for " + fileLabel + ": " + contentType +
                            ". Allowed types: " + String.join(", ", allowedTypes)
            );
        }

        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException(
                    fileLabel + " exceeds maximum size of " + (maxSizeBytes / 1024 / 1024) + "MB."
            );
        }
    }
}
