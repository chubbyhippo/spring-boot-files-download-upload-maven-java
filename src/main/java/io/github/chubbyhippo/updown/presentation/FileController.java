package io.github.chubbyhippo.updown.presentation;

import io.github.chubbyhippo.updown.application.FileService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Stream;

@RestController
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/uploadFile")
    public String upload(@RequestParam("file") MultipartFile file) {
        fileService.uploadFile(file);
        return "File uploaded successfully: " + file.getOriginalFilename();
    }


    @PostMapping("/uploadFiles")
    public String upload(@RequestParam("file") Stream<MultipartFile> files) {
        files.forEach(fileService::uploadFile);
        return "Files uploaded successfully";
    }
}