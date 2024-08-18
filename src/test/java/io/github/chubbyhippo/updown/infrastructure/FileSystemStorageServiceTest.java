package io.github.chubbyhippo.updown.infrastructure;

import io.github.chubbyhippo.updown.domain.StorageException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class FileSystemStorageServiceTest {


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

            var service = new FileSystemStorageService(new StorageProperties("test"));

            assertThatThrownBy(service::init)
                    .isInstanceOf(StorageException.class)
                    .hasMessage("Could not initialize storage");

        }

    }

}