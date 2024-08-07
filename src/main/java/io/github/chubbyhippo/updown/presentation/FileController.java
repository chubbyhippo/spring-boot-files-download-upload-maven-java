package io.github.chubbyhippo.updown.presentation;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileController {

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) {
        return "File uploaded successfully: " + file.getOriginalFilename();

    }
}