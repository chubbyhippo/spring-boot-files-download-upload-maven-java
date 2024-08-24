package io.github.chubbyhippo.updown.application;

import io.github.chubbyhippo.updown.domain.EmptyFileException;
import io.github.chubbyhippo.updown.domain.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.file.Path;
import java.util.stream.Stream;

@Service
public class FileService {

    private final StorageService storageService;

    public FileService(StorageService storageService) {
        this.storageService = storageService;
    }

    public void uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new EmptyFileException("Cannot upload empty file.");
        }
        storageService.store(file);
    }

    public Stream<String> listFiles() {
        return storageService.loadAll()
                .map(Path::toString);
    }

    public Resource loadAsResource(String filename) {
        return storageService.loadAsResource(filename);
    }

    public StreamingResponseBody zipFiles(Stream<String> filenames) {
        return storageService.zipFiles(filenames);
    }
}
