package io.github.chubbyhippo.updown;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.chubbyhippo.updown.domain.StorageService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Execution(ExecutionMode.SAME_THREAD)
public class UpDownApplicationMockMvcTesterIntegrationTests {

    @Autowired
    private MockMvcTester mockMvcTester;
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

    @AfterEach
    void tearDown() {
        storageService.deleteAll();
    }

    @Test
    @DisplayName("should upload file")
    void shouldUploadFile() {
        var mockMultipartFile = new MockMultipartFile(
                "file",
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );

        assertThat(mockMvcTester.post().uri("/file")
                .multipart()
                .file(mockMultipartFile))
                .hasStatusOk()
                .hasContentTypeCompatibleWith(MediaType.TEXT_PLAIN)
                .bodyText().isEqualTo("File uploaded successfully: hello.txt");
    }

    @Test
    @DisplayName("should upload files")
    void shouldUploadFiles() {
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

        assertThat(mockMvcTester.post().uri("/files")
                .multipart()
                .file(file1)
                .file(file2))
                .hasStatusOk()
                .bodyText().isEqualTo("Files uploaded successfully");
    }

    @Test
    @DisplayName("should list files")
    void shouldListFiles() throws Exception {
        var path1 = tempDir.resolve("testList1.txt");
        Files.write(path1, "test1".getBytes());

        var path2 = tempDir.resolve("testList2.txt");
        Files.write(path2, "test2".getBytes());

        var result = mockMvcTester.get().uri("/files")
                .accept(MediaType.APPLICATION_JSON)
                .exchange();

        assertThat(result)
                .hasStatusOk()
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_JSON);

        var json = result.getResponse().getContentAsString();
        var files = objectMapper.readValue(json, String[].class);

        assertThat(files).isNotEmpty()
                .contains("testList1.txt", "testList2.txt");
    }

    @Test
    @DisplayName("should download file")
    void shouldDownloadFile() throws Exception {
        var filename = "testfile.txt";
        var path = tempDir.resolve(filename);
        Files.write(path, "test".getBytes());

        assertThat(mockMvcTester.get().uri("/files/{filename}", filename))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_OCTET_STREAM)
                .headers().hasValue(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + filename);
    }

    @Test
    @DisplayName("should return bad request when upload an empty file")
    void shouldReturnBadRequestWhenUploadAnEmptyFile() {
        var mockMultipartFile = new MockMultipartFile(
                "file",
                "empty.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "".getBytes()
        );

        assertThat(mockMvcTester.post().uri("/file")
                .multipart()
                .file(mockMultipartFile))
                .hasStatus(400)
                .bodyText().isEqualTo("Cannot upload empty file.");
    }

    @Test
    @DisplayName("should return zip")
    void shouldReturnZip() throws Exception {
        var filenames = List.of("test1.txt", "test2.txt");

        var path1 = tempDir.resolve(filenames.getFirst());
        Files.write(path1, "test1".getBytes());
        var path2 = tempDir.resolve(filenames.getLast());
        Files.write(path2, "test2".getBytes());

        var jsonFilenames = objectMapper.writeValueAsString(filenames);

        assertThat(mockMvcTester.post().uri("/zip")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .content(jsonFilenames))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_OCTET_STREAM)
                .headers().hasValue(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"files.zip\"");
    }
}