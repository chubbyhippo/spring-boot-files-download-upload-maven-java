package io.github.chubbyhippo.updown;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.chubbyhippo.updown.infrastructure.StorageService;
import org.junit.jupiter.api.AfterEach;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Execution(ExecutionMode.CONCURRENT)
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
        storageService.init();
    }

    @AfterEach
    void tearDown() {
        storageService.deleteAll();
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

    @Test
    @DisplayName("should list files")
    void shouldListFiles() throws Exception {
        var path1 = tempDir.resolve(tempDir + "/testList1.txt");
        Files.write(path1, "test1".getBytes());

        var path2 = tempDir.resolve(tempDir + "/testList2.txt");
        Files.write(path2, "test2".getBytes());

        var mvcResult = mockMvc.perform(get("/listFiles")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        var json = mvcResult.getResponse().getContentAsString();
        var contentAsString = objectMapper.readValue(json, String[].class);

        assertThat(contentAsString).isEqualTo(new String[]{"testList1.txt", "testList2.txt"});

    }
}
