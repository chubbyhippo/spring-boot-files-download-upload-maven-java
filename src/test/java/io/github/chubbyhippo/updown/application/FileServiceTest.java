package io.github.chubbyhippo.updown.application;

import io.github.chubbyhippo.updown.domain.EmptyFileException;
import io.github.chubbyhippo.updown.domain.StorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private StorageService storageService;
    @InjectMocks
    private FileService fileService;

    @Test
    @DisplayName("should upload a file")
    void shouldUploadAFile() {
        var file = mock(MultipartFile.class);
        fileService.uploadFile(file);
        verify(storageService).store(file);
    }

    @Test
    @DisplayName("should list files")
    void shouldListFiles() {
        fileService.listFiles();
        verify(storageService).loadAll();
    }

    @Test
    @DisplayName("should load as resource")
    void shouldLoadAsResource() {
        var file = "test.txt";
        fileService.loadAsResource(file);
        verify(storageService).loadAsResource(file);
    }

    @Test
    @DisplayName("should throw if upload empty file")
    void shouldThrowIfUploadEmptyFile() {
        var file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        assertThatThrownBy(() -> fileService.uploadFile(file))
                .isInstanceOf(EmptyFileException.class);
    }

}