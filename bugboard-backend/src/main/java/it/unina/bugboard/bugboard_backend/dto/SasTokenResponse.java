package it.unina.bugboard.bugboard_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SasTokenResponse {
    private String uploadUrl;
    private String blobFileName;
}