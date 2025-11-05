package org.parkcontrol.apiparkcontrol.config;

import org.mockito.Mockito;
import org.parkcontrol.apiparkcontrol.services.filestorage.FileStorageService;
import org.parkcontrol.apiparkcontrol.services.filestorage.S3StorageService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@TestConfiguration
public class TestStorageConfiguration {

    @Bean
    @Primary
    public FileStorageService mockFileStorageService() {
        FileStorageService mock = Mockito.mock(FileStorageService.class);
        try {
            Mockito.when(mock.getUrl(Mockito.any(MultipartFile.class)))
                    .thenReturn("uploads/test-file.jpg");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return mock;
    }

    @Bean
    @Primary
    public S3StorageService mockS3StorageService() {
        S3StorageService mock = Mockito.mock(S3StorageService.class);
        try {
            Mockito.when(mock.uploadToS3(Mockito.any(MultipartFile.class)))
                    .thenReturn("s3-key/test-file.jpg");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return mock;
    }
}
