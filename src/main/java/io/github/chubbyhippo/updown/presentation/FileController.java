package io.github.chubbyhippo.updown.presentation;

import io.github.chubbyhippo.updown.application.FileService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
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

    @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String upload(@RequestPart("file") List<MultipartFile> files) {
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

        var encodedFilename = URLEncoder.encode(Objects.requireNonNull(resource.getFilename()), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFilename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PostMapping("/zip")
    public ResponseEntity<StreamingResponseBody> zipFiles(@RequestBody List<String> filenames) {

        var streamingResponseBody = fileService.zipFiles(filenames.stream());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"files.zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(streamingResponseBody);

    }

}