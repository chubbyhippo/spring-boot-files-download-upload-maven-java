package io.github.chubbyhippo.updown.infrastructure;

import io.github.chubbyhippo.updown.domain.EmptyFileException;
import io.github.chubbyhippo.updown.domain.StorageException;
import io.github.chubbyhippo.updown.domain.StorageFileNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileSystemStorageServiceTest {

    @TempDir
    private Path tempDir;

    @Test
    @DisplayName("should throw exception when root path is empty")
    void shouldThrowExceptionWhenRootPathIsEmpty() {
        var properties = new StorageProperties("");
        assertThatThrownBy(() -> new FileSystemStorageService(properties))
                .isInstanceOf(StorageException.class)
                .hasMessage("File upload location can not be Empty.");

    }

    @Test
    @DisplayName("should throw exception when cannot create dir")
    void shouldThrowExceptionWhenCannotCreateDir() {

        try (var filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.createDirectories(any(Path.class)))
                    .thenThrow(new IOException());

            var service = new FileSystemStorageService(new StorageProperties(tempDir.toString()));

            assertThatThrownBy(service::init)
                    .isInstanceOf(StorageException.class)
                    .hasMessage("Could not initialize storage");

        }

    }

    @Test
    @DisplayName("should throw exception when uploading an empty file")
    void shouldThrowExceptionWhenUploadingAnEmptyFile() {

        var service = new FileSystemStorageService(new StorageProperties(tempDir.toString()));

        var multipartFile = mock(MultipartFile.class);
        when(multipartFile.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> service.store(multipartFile))
                .isInstanceOf(EmptyFileException.class)
                .hasMessage("Failed to store empty file.");

    }

    @Test
    @DisplayName("should throw exception when storing file outside the root")
    void shouldThrowExceptionWhenStoringFileOutsideTheRoot() {

        var service = new FileSystemStorageService(new StorageProperties(tempDir.toString()));

        var multipartFile = mock(MultipartFile.class);
        when(multipartFile.getOriginalFilename()).thenReturn("../file.txt");

        assertThatThrownBy(() -> service.store(multipartFile))
                .isInstanceOf(StorageException.class)
                .hasMessage("Cannot store file outside current directory.");
    }

    @Test
    @DisplayName("should throw exception when cannot store file")
    void shouldThrowExceptionWhenCannotStoreFile() throws IOException {

        var service = new FileSystemStorageService(new StorageProperties(tempDir.toString()));

        var multipartFile = mock(MultipartFile.class);
        when(multipartFile.getOriginalFilename()).thenReturn("file.txt");
        when(multipartFile.getInputStream()).thenThrow(new IOException());

        assertThatThrownBy(() -> service.store(multipartFile))
                .isInstanceOf(StorageException.class)
                .hasMessage("Failed to store file.");
    }

    @Test
    @DisplayName("should throw exception when file not found")
    void shouldThrowExceptionWhenFileNotFound() {
        var filename = "nonexistentfile.txt";
        var service = new FileSystemStorageService(new StorageProperties("test.txt"));

        assertThatThrownBy(() -> service.loadAsResource(filename))
                .isInstanceOf(StorageFileNotFoundException.class)
                .hasMessage("Could not read file: %s".formatted(filename));
    }

    @Test
    @DisplayName("should throw exception when url is malformed")
    void shouldThrowExceptionWhenUrlIsMalformed() {

        var filename = "../malformedfile.txt";
        var service = new FileSystemStorageService(new StorageProperties("test.txt"));

        assertThatThrownBy(() -> service.loadAsResource(filename))
                .isInstanceOf(StorageFileNotFoundException.class)
                .hasMessage("Could not read file: %s".formatted(filename));
    }

    @Test
    @DisplayName("should return stream of path")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void shouldReturnStreamOfPath() {
        var service = new FileSystemStorageService(new StorageProperties(tempDir.toString()));
        tempDir.resolve("test.txt");
        assertThat(service.loadAll()).isNotNull();
    }

    @Test
    @DisplayName("should return zipped streaming response body")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void shouldReturnZippedStreamingResponseBody() {

        var service = new FileSystemStorageService(new StorageProperties(tempDir.toString()));
        tempDir.resolve("test1.txt");
        tempDir.resolve("test2.txt");

        var streamingResponseBody = service.zipFiles(Stream.of("test1.txt", "test2.txt"));
        assertThat(streamingResponseBody).isNotNull();

    }
}