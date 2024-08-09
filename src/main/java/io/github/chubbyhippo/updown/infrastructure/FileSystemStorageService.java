package io.github.chubbyhippo.updown.infrastructure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class FileSystemStorageService {
    private final Path rootLocation;

    public FileSystemStorageService(String location) {
        this.rootLocation = Paths.get(location);
    }

    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

    public void store(String filename, byte[] fileContent) {
        try {
            var destinationFile = rootLocation.resolve(Paths.get(filename))
                    .normalize().toAbsolutePath();
            Files.write(destinationFile, fileContent);
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + filename, e);
        }
    }

    public byte[] read(String filename) {
        try {
            var filePath = rootLocation.resolve(filename).normalize().toAbsolutePath();
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new StorageException("Failed to read file " + filename, e);
        }
    }

    public void delete(String filename) {
        try {
            var filePath = rootLocation.resolve(filename).normalize().toAbsolutePath();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new StorageException("Failed to delete file " + filename, e);
        }
    }

    public Stream<Path> listFiles() {
        try {
            return Files.walk(rootLocation, 1)
                    .filter(path -> !path.equals(rootLocation))
                    .map(rootLocation::relativize);
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }
    }
}