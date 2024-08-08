package io.github.chubbyhippo.updown.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;

class FileSystemStorageServiceTest {
    private FileSystemStorageService storageService;

    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() {
        storageService = new FileSystemStorageService(tempDir.toString());
        storageService.init();
    }

    @Test
    @DisplayName("should store and read file")
    void shouldStoreAndReadFile() {

        var filename = "test.txt";
        var content = "Hello, World!".getBytes();

        storageService.store(filename, content);
        var readContent = storageService.read(filename);

        assertThat(readContent).isEqualTo(content);
    }

    @Test
    @DisplayName("should delete file")
    void shouldDeleteFile() {
        var filename = "test.txt";
        var content = "Hello, World!".getBytes();

        storageService.store(filename, content);
        storageService.delete(filename);

        assertThatThrownBy(() -> storageService.read(filename))
                .isInstanceOf(FileSystemStorageService.StorageException.class)
                .hasMessageContaining("Failed to read file");
    }

    @Test
    @DisplayName("should return list of file")
    void shouldReturnListOfFile() {
        storageService.store("file1.txt", "Content 1".getBytes());
        storageService.store("file2.txt", "Content 2".getBytes());

        var files = storageService.listFiles().toList();

        assertThat(files).hasSize(2)
                .contains(Path.of("file1.txt"), Path.of("file2.txt"));
    }
}