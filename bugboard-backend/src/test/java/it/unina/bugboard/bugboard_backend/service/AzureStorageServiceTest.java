package it.unina.bugboard.bugboard_backend.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import it.unina.bugboard.bugboard_backend.dto.SasTokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AzureStorageServiceTest {

    private AzureStorageService azureStorageService;

    private BlobContainerClient containerClient;
    private BlobClient blobClient;

    @BeforeEach
    void setUp() throws Exception {
        azureStorageService = new AzureStorageService();

        // Inject test values via reflection (simulates @Value injection)
        setField(azureStorageService, "connectionString", "DefaultEndpointsProtocol=https;AccountName=test;AccountKey=dGVzdA==;EndpointSuffix=core.windows.net");
        setField(azureStorageService, "containerName", "test-container");

        // Pre-inject a mocked containerClient so lazy init is skipped
        containerClient = mock(BlobContainerClient.class);
        blobClient = mock(BlobClient.class);
        setField(azureStorageService, "containerClient", containerClient);
    }

    @Test
    void generateUploadSasUrl_ReturnsValidResponse() {
        when(containerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.getBlobUrl()).thenReturn("https://test.blob.core.windows.net/test-container/uuid-file.txt");
        when(blobClient.generateSas(any())).thenReturn("sv=2021-06-08&sig=abc");

        SasTokenResponse response = azureStorageService.generateUploadSasUrl("document.txt");

        assertNotNull(response);
        assertNotNull(response.getUploadUrl());
        assertTrue(response.getUploadUrl().contains("sv=2021-06-08&sig=abc"));
        assertNotNull(response.getBlobFileName());
        assertTrue(response.getBlobFileName().endsWith(".txt"));
    }

    @Test
    void generateUploadSasUrl_PreservesExtension() {
        when(containerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.getBlobUrl()).thenReturn("https://test.blob.core.windows.net/test-container/uuid.pdf");
        when(blobClient.generateSas(any())).thenReturn("token");

        SasTokenResponse response = azureStorageService.generateUploadSasUrl("report.pdf");

        assertTrue(response.getBlobFileName().endsWith(".pdf"));
    }

    @Test
    void generateUploadSasUrl_HandlesFileWithoutExtension() {
        when(containerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.getBlobUrl()).thenReturn("https://test.blob.core.windows.net/test-container/uuid");
        when(blobClient.generateSas(any())).thenReturn("token");

        SasTokenResponse response = azureStorageService.generateUploadSasUrl("Makefile");

        assertNotNull(response.getBlobFileName());
        assertFalse(response.getBlobFileName().contains("."));
    }

    @Test
    void generateUploadSasUrl_HandlesNullFilename() {
        when(containerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.getBlobUrl()).thenReturn("https://test.blob.core.windows.net/test-container/uuid");
        when(blobClient.generateSas(any())).thenReturn("token");

        SasTokenResponse response = azureStorageService.generateUploadSasUrl(null);

        assertNotNull(response.getBlobFileName());
    }

    @Test
    void generateDownloadSasUrl_ReturnsValidUrl() {
        when(containerClient.getBlobClient("existing-blob.png")).thenReturn(blobClient);
        when(blobClient.getBlobUrl()).thenReturn("https://test.blob.core.windows.net/test-container/existing-blob.png");
        when(blobClient.generateSas(any())).thenReturn("sv=2021-06-08&sig=xyz");

        String url = azureStorageService.generateDownloadSasUrl("existing-blob.png");

        assertNotNull(url);
        assertTrue(url.startsWith("https://test.blob.core.windows.net/test-container/existing-blob.png?"));
        assertTrue(url.contains("sv=2021-06-08&sig=xyz"));
    }

    @Test
    void getContainerClient_InitializesLazily() throws Exception {
        AzureStorageService freshService = new AzureStorageService();
        setField(freshService, "connectionString", "DefaultEndpointsProtocol=https;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://127.0.0.1:10000/devstoreaccount1");
        setField(freshService, "containerName", "test-container");

        // containerClient field should be null before first use
        Field field = AzureStorageService.class.getDeclaredField("containerClient");
        field.setAccessible(true);
        assertNull(field.get(freshService));
    }

    @Test
    void getContainerClient_CreatesContainerWhenNotExists() throws Exception {
        BlobServiceClient mockServiceClient = mock(BlobServiceClient.class);
        BlobContainerClient mockContainer = mock(BlobContainerClient.class);
        BlobClient mockBlob = mock(BlobClient.class);

        when(mockServiceClient.getBlobContainerClient("test-container")).thenReturn(mockContainer);
        when(mockContainer.exists()).thenReturn(false);
        when(mockContainer.getBlobClient(anyString())).thenReturn(mockBlob);
        when(mockBlob.getBlobUrl()).thenReturn("https://test.blob.core.windows.net/test-container/file.txt");
        when(mockBlob.generateSas(any())).thenReturn("token");

        try (MockedConstruction<BlobServiceClientBuilder> mocked = mockConstruction(BlobServiceClientBuilder.class,
                (builder, context) -> {
                    when(builder.connectionString(anyString())).thenReturn(builder);
                    when(builder.buildClient()).thenReturn(mockServiceClient);
                })) {

            AzureStorageService freshService = new AzureStorageService();
            setField(freshService, "connectionString", "DefaultEndpointsProtocol=https;AccountName=test;AccountKey=dGVzdA==;EndpointSuffix=core.windows.net");
            setField(freshService, "containerName", "test-container");

            SasTokenResponse response = freshService.generateUploadSasUrl("file.txt");

            assertNotNull(response);
            verify(mockContainer).create();
        }
    }

    @Test
    void getContainerClient_SkipsCreateWhenContainerExists() throws Exception {
        BlobServiceClient mockServiceClient = mock(BlobServiceClient.class);
        BlobContainerClient mockContainer = mock(BlobContainerClient.class);
        BlobClient mockBlob = mock(BlobClient.class);

        when(mockServiceClient.getBlobContainerClient("test-container")).thenReturn(mockContainer);
        when(mockContainer.exists()).thenReturn(true);
        when(mockContainer.getBlobClient(anyString())).thenReturn(mockBlob);
        when(mockBlob.getBlobUrl()).thenReturn("https://test.blob.core.windows.net/test-container/file.txt");
        when(mockBlob.generateSas(any())).thenReturn("token");

        try (MockedConstruction<BlobServiceClientBuilder> mocked = mockConstruction(BlobServiceClientBuilder.class,
                (builder, context) -> {
                    when(builder.connectionString(anyString())).thenReturn(builder);
                    when(builder.buildClient()).thenReturn(mockServiceClient);
                })) {

            AzureStorageService freshService = new AzureStorageService();
            setField(freshService, "connectionString", "DefaultEndpointsProtocol=https;AccountName=test;AccountKey=dGVzdA==;EndpointSuffix=core.windows.net");
            setField(freshService, "containerName", "test-container");

            SasTokenResponse response = freshService.generateUploadSasUrl("file.txt");

            assertNotNull(response);
            verify(mockContainer, never()).create();
        }
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
