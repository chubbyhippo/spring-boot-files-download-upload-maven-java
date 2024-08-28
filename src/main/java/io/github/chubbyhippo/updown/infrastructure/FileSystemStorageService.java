package io.github.chubbyhippo.updown.infrastructure;

import io.github.chubbyhippo.updown.domain.EmptyFileException;
import io.github.chubbyhippo.updown.domain.StorageException;
import io.github.chubbyhippo.updown.domain.StorageFileNotFoundException;
import io.github.chubbyhippo.updown.domain.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;

    public FileSystemStorageService(StorageProperties properties) {

        if (properties.location().trim().isEmpty()) {
            throw new StorageException("File upload location can not be Empty.");
        }

        this.rootLocation = Paths.get(properties.location());
    }

    public void init() throws StorageException {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

    @Override
    public void store(MultipartFile file) throws EmptyFileException, StorageException {
        try {
            if (file.isEmpty()) {
                throw new EmptyFileException("Failed to store empty file.");
            }
            var path = rootLocation.resolve(
                            Path.of(Objects.requireNonNull(file.getOriginalFilename())))
                    .normalize().toAbsolutePath();
            if (!path.getParent().equals(rootLocation.toAbsolutePath())) {
                // This is a security check
                throw new StorageException(
                        "Cannot store file outside current directory.");
            }
            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
    }

    @SuppressWarnings("resource")
    @Override
    public Stream<Path> loadAll() throws StorageException {
        var directoryTraversalDepth = 1;
        try {
            return Files.walk(rootLocation, directoryTraversalDepth)
                    .filter(path -> !path.equals(rootLocation))
                    .map(rootLocation::relativize);
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    @Override
    public Resource loadAsResource(String filename) throws StorageFileNotFoundException {
        try {
            var path = rootLocation.resolve(filename);
            var resource = new UrlResource(path.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException(
                        "Could not read file: %s".formatted(filename));

            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: %s".formatted(filename), e);
        }
    }

    @Override
    public StreamingResponseBody zipFiles(Stream<String> filenames) {
        return outputStream -> {
            try (var zos = new ZipOutputStream(outputStream)) {
                filenames.forEach(filename -> {
                    var filePath = rootLocation.resolve(filename);
                    try (var fis = Files.newInputStream(filePath)) {
                        zos.putNextEntry(new ZipEntry(filename));
                        var buffer = new byte[4096];
                        int len;
                        while ((len = fis.read(buffer)) != -1) {
                            zos.write(buffer, 0, len);
                        }
                        zos.closeEntry();
                    } catch (IOException e) {
                        throw new StorageException("Failed to read file: %s".formatted(filename), e);
                    }
                });
            }
        };
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

}