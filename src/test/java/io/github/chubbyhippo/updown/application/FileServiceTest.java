package io.github.chubbyhippo.updown.application;

import io.github.chubbyhippo.updown.domain.StorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private StorageService storageService;

    @InjectMocks
    private FileService fileService;

    @Test
    @DisplayName("should upload a file")
    void shouldUploadAFile() {
        var file = Mockito.mock(MultipartFile.class);
        fileService.uploadFile(file);
        verify(storageService).store(file);
    }

}