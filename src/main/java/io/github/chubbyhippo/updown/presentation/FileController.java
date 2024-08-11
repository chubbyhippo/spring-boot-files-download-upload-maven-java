package io.github.chubbyhippo.updown.presentation;

import io.github.chubbyhippo.updown.application.FileService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Stream;

@RestController
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/file")
    public String upload(@RequestParam("file") MultipartFile file) {
        fileService.uploadFile(file);
        return "File uploaded successfully: " + file.getOriginalFilename();
    }

    @PostMapping("/files")
    public String upload(@RequestParam("file") Stream<MultipartFile> files) {
        files.forEach(fileService::uploadFile);
        return "Files uploaded successfully";
    }

    @GetMapping("/files")
    public Stream<String> listFiles() {
        return fileService.listFiles();
    }

    @GetMapping(value = "/files/{filename:.+}", produces = "application/octet-stream", headers = "Content-Disposition=attachment; filename={filename}")
    public Resource serveFile(@PathVariable String filename) {
        return fileService.loadAsResource(filename);
    }

}