package it.unina.bugboard.bugboard_backend.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record AttachmentResponse(
        UUID id,
        String fileName,
        String filePath,
        Long fileSize,
        String fileExtension,
        UUID issueId
) {
}