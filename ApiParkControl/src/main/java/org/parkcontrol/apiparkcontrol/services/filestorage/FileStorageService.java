package org.parkcontrol.apiparkcontrol.services.filestorage;


import org.parkcontrol.apiparkcontrol.utils.GeneradorCodigo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    @Value("${storage.path.local}")
    private String storagePathLocal;


    private final GeneradorCodigo generadorCodigo = new GeneradorCodigo();


    public String getUrl(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String pathFile = generadorCodigo.getCode().replace("-", "") + originalFilename;
        Path path = Paths.get(storagePathLocal, pathFile);
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        return pathFile;
    }


}