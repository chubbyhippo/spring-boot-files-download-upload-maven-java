package io.github.chubbyhippo.updown.infrastructure;

import io.github.chubbyhippo.updown.domain.EmptyFileException;
import io.github.chubbyhippo.updown.domain.StorageException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileSystemStorageServiceTest {

    @TempDir
    private Path tempDir;

    @Test
    @DisplayName("should throw exception when root path is empty")
    void shouldThrowExceptionWhenRootPathIsEmpty() {
        assertThatThrownBy(() -> new FileSystemStorageService(new StorageProperties("")))
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

}