package io.github.chubbyhippo.updown.domain;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public interface StorageService {
    void init() throws StorageException;

    void store(MultipartFile file) throws EmptyFileException, StorageException;

    Stream<Path> loadAll() throws StorageException;

    Resource loadAsResource(String filename) throws StorageFileNotFoundException;

    StreamingResponseBody zipFiles(Stream<String> filenames);

    void deleteAll();
}
