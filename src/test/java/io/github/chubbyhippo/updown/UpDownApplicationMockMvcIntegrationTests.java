package io.github.chubbyhippo.updown;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UpDownApplicationMockMvcIntegrationTests {

    @Autowired
    private MockMvc mockMvc;
    @TempDir
    private static Path tempDir;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("storage.location", () -> tempDir.toString());
    }

    @Test
    @DisplayName("should upload file")
    void shouldUploadFile() throws Exception {
        var mockMultipartFile = new MockMultipartFile(
                "file",
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        mockMvc.perform(multipart("/uploadFile")
                        .file(mockMultipartFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("File uploaded successfully: hello.txt"));

    }

    @Test
    @DisplayName("should upload files")
    void shouldUploadFiles() throws Exception {
        var file1 = new MockMultipartFile(
                "file",
                "test1.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "test1".getBytes()
        );

        var file2 = new MockMultipartFile(
                "file",
                "test2.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "test2".getBytes()
        );
        mockMvc.perform(multipart("/uploadFiles")
                        .file(file1)
                        .file(file2))
                .andExpect(status().isOk())
                .andExpect(content().string("Files uploaded successfully"));

    }
}
