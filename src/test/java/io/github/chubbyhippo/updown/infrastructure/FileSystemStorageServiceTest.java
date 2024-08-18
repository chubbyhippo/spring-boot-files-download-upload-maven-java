package io.github.chubbyhippo.updown.infrastructure;

import io.github.chubbyhippo.updown.domain.StorageException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class FileSystemStorageServiceTest {


    @Test
    @DisplayName("should throw exception when root path is empty")
    void shouldThrowExceptionWhenRootPathIsEmpty() {
        assertThatThrownBy(() -> new FileSystemStorageService(new StorageProperties("")))
                .isInstanceOf(StorageException.class)
                .hasMessage("File upload location can not be Empty.");

    }

}