package com.example.demo.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.entity.User;
import com.example.demo.exception.FileUploadException;
import com.example.demo.security.UserPrincipal;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-key}")
    private String supabaseServiceKey;

    @Value("${supabase.bucket.name}")
    private String bucketName;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private final WebClient webClient;

    public SupabaseStorageService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Uploads a profile photo to Supabase Storage
     * @param file The image file to upload
     * @return The public URL of the uploaded image
     * @throws IOException If file reading fails
     * @throws FileUploadException If upload fails or validation fails
     */
    public String uploadProfilePhoto(MultipartFile file) throws IOException {
        // Validate file
        validateFile(file);

        // Get current user
        User user = UserPrincipal.getCurrentUser();

        // Generate unique filename with user folder structure
        String fileName = generateFileName(file.getOriginalFilename());
        String filePath = user.getId() + "/" + fileName;

        try {
            // Upload to Supabase Storage
            String response = webClient.post()
                    .uri(supabaseUrl + "/storage/v1/object/" + bucketName + "/" + filePath)
                    .header("Authorization", "Bearer " + supabaseServiceKey)
                    .contentType(MediaType.parseMediaType(file.getContentType()))
                    .bodyValue(file.getBytes())
                    .retrieve()
                    .onStatus(
                            status -> status.value() >= 400,
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(body -> new FileUploadException("Failed to upload image: " + body))
                    )
                    .bodyToMono(String.class)
                    .block();

            log.info("Successfully uploaded image for user: {} to path: {}", user.getEmail(), filePath);

            // Return public URL
            return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + filePath;

        } catch (Exception e) {
            log.error("Error uploading file to Supabase: ", e);
            throw new FileUploadException("Failed to upload image to storage: " + e.getMessage());
        }
    }

    /**
     * Validates the uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("File cannot be empty");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileUploadException("File size exceeds maximum limit of 5MB");
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new FileUploadException(
                    "Invalid file type. Only JPEG, PNG, and WebP images are allowed"
            );
        }
    }

    /**
     * Generates a unique filename
     */
    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return "profile-" + UUID.randomUUID().toString() + extension;
    }

    /**
     * Deletes a file from Supabase Storage (for future use)
     */
    public void deleteFile(String fileUrl) {
        try {
            // Extract file path from URL
            String filePath = fileUrl.replace(
                    supabaseUrl + "/storage/v1/object/public/" + bucketName + "/", ""
            );

            webClient.delete()
                    .uri(supabaseUrl + "/storage/v1/object/" + bucketName + "/" + filePath)
                    .header("Authorization", "Bearer " + supabaseServiceKey)
                    .retrieve()
                    .onStatus(
                            status -> status.value() >= 400 && status.value() != 404,
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(body -> new FileUploadException("Failed to delete image: " + body))
                    )
                    .bodyToMono(String.class)
                    .block();

            log.info("Successfully deleted image at path: {}", filePath);

        } catch (Exception e) {
            log.error("Error deleting file from Supabase: ", e);
            // Don't throw exception for delete failures (non-critical)
        }
    }
}
