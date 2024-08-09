package io.github.chubbyhippo.updown.application;

import io.github.chubbyhippo.updown.infrastructure.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    private final StorageService storageService;

    public FileService(StorageService storageService) {
        this.storageService = storageService;
    }

    public void uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Cannot upload empty file.");
        }
        storageService.store(file);
    }
}
