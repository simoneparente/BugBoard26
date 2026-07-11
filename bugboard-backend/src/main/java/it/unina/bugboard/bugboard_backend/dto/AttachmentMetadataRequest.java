package it.unina.bugboard.bugboard_backend.dto;

import lombok.Data;

@Data
public class AttachmentMetadataRequest {
    private String originalFileName;
    private String blobFileName; //Returned by the Azure Storage Service when generating the SAS token
    private long fileSize;
    private String extension;
}
