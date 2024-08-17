package io.github.chubbyhippo.updown.domain;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {
    void init() throws StorageException;

    void store(MultipartFile file) throws EmptyFileException, StorageException;

    Stream<Path> loadAll() throws StorageException;

    Path load(String filename);

    Resource loadAsResource(String filename) throws StorageFileNotFoundException;

    void deleteAll();
}
