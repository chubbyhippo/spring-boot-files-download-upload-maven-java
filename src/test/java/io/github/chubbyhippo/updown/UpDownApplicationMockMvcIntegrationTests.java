package io.github.chubbyhippo.updown;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.chubbyhippo.updown.domain.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Execution(ExecutionMode.SAME_THREAD)
class UpDownApplicationMockMvcIntegrationTests {

    @Autowired
    private MockMvc mockMvc;
    @TempDir
    private static Path tempDir;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private StorageService storageService;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("storage.location", () -> tempDir.toString());
    }

    @BeforeEach
    void setUp() {
        storageService.deleteAll();
        storageService.init();
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
        mockMvc.perform(multipart("/file")
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
        mockMvc.perform(multipart("/files")
                        .file(file1)
                        .file(file2))
                .andExpect(status().isOk())
                .andExpect(content().string("Files uploaded successfully"));

    }

    @Test
    @DisplayName("should list files")
    void shouldListFiles() throws Exception {
        var path1 = tempDir.resolve(tempDir + "/testList1.txt");
        Files.write(path1, "test1".getBytes());

        var path2 = tempDir.resolve(tempDir + "/testList2.txt");
        Files.write(path2, "test2".getBytes());

        var mvcResult = mockMvc.perform(get("/files")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        var json = mvcResult.getResponse().getContentAsString();
        var files = objectMapper.readValue(json, String[].class);

        assertThat(files).isNotEmpty();
        assertThat(files).contains("testList1.txt", "testList2.txt");

    }

    @Test
    @DisplayName("should download file")
    void shouldDownloadFile() throws Exception {
        var filename = "testfile.txt";
        var path = tempDir.resolve(tempDir + "/" + filename);
        Files.write(path, "test".getBytes());

        mockMvc.perform(get("/files/{filename}", filename))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=" + "\"" + filename + "\""));
    }

    @Test
    @DisplayName("should return bad request when upload an empty file")
    void shouldReturnBadRequestWhenUploadAnEmptyFile() throws Exception {
        var mockMultipartFile = new MockMultipartFile(
                "file",
                "empty.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "".getBytes()
        );
        mockMvc.perform(multipart("/file")
                        .file(mockMultipartFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cannot upload empty file."));
    }
}
