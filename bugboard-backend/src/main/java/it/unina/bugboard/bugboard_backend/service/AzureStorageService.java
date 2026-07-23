package it.unina.bugboard.bugboard_backend.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import it.unina.bugboard.bugboard_backend.dto.SasTokenResponse;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class AzureStorageService {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.container-name}")
    private String containerName;

    private BlobContainerClient containerClient;

    private synchronized BlobContainerClient getContainerClient() {
        if (containerClient != null) {
            return containerClient;
        }

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        BlobContainerClient client = blobServiceClient.getBlobContainerClient(containerName);

        if (!client.exists()) {
            client.create();
        }

        this.containerClient = client;
        return containerClient;
    }

    /**
     * Generate an URL SAS to allow the file UPLOAD (Permission: WRITE).
     *
     * @param originalFilename originalFilename
     * @return DTO containing the upload URL and the unique blob file name
     */
    public SasTokenResponse generateUploadSasUrl(String originalFilename) {
        String uniqueFileName = generateUniqueFileName(originalFilename);
        BlobClient blobClient = getContainerClient().getBlobClient(uniqueFileName);

        // Set the permissions for the SAS token (WRITE and CREATE)
        BlobSasPermission sasPermission = new BlobSasPermission()
                .setCreatePermission(true)
                .setWritePermission(true);

        // Set the expiry time for the SAS token (e.g., 10 minutes from now)
        OffsetDateTime expiryTime = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(10);

        BlobServiceSasSignatureValues sasSignatureValues = new BlobServiceSasSignatureValues(expiryTime, sasPermission);

        // Generate the SAS token
        String sasToken = blobClient.generateSas(sasSignatureValues);
        
        // Construct the final URL for the upload
        String uploadUrl = blobClient.getBlobUrl() + "?" + sasToken;

        return new SasTokenResponse(uploadUrl, uniqueFileName);
    }
    
     /**
     * Generate a SAS URL to allow the frontend to DOWNLOAD or view a file (Permission: READ).
     */
    public String generateDownloadSasUrl(String blobFileName) {
        BlobClient blobClient = getContainerClient().getBlobClient(blobFileName);

        BlobSasPermission sasPermission = new BlobSasPermission().setReadPermission(true);
        OffsetDateTime expiryTime = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(15);

        BlobServiceSasSignatureValues sasSignatureValues = new BlobServiceSasSignatureValues(expiryTime, sasPermission);

        return blobClient.getBlobUrl() + "?" + blobClient.generateSas(sasSignatureValues);
    }

    private String generateUniqueFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}