package io.github.chubbyhippo.updown.presentation;

import io.github.chubbyhippo.updown.application.FileService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) {
        fileService.uploadFile(file);
        return "File uploaded successfully: " + file.getOriginalFilename();
    }
}