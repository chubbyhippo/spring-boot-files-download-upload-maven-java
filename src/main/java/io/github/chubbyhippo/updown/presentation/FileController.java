package io.github.chubbyhippo.updown.presentation;

import io.github.chubbyhippo.updown.application.FileService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String upload(@RequestPart("file") MultipartFile file) {
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

    @GetMapping(value = "/files/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        var resource = fileService.loadAsResource(filename);

        if (resource == null)
            return ResponseEntity.notFound()
                    .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

}