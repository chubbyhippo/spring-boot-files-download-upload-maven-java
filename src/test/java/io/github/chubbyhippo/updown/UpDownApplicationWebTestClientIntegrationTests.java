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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Execution(ExecutionMode.CONCURRENT)
class UpDownApplicationWebTestClientIntegrationTests {

    @Autowired
    private WebTestClient webTestClient;
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
    void shouldUploadFile() {

        var filename = "hello.txt";
        var multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("file", "test".getBytes())
                .contentType(MediaType.TEXT_PLAIN)
                .filename(filename);

        webTestClient.post()
                .uri("/file")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("File uploaded successfully: %s".formatted(filename));
    }

    @Test
    @DisplayName("should upload files")
    void shouldUploadFiles() throws IOException {
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

        MultiValueMap<String, HttpEntity<?>> body = new LinkedMultiValueMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<Resource> filePart1 = new HttpEntity<>(new ByteArrayResource(file1.getBytes()), headers);
        HttpEntity<Resource> filePart2 = new HttpEntity<>(new ByteArrayResource(file2.getBytes()), headers);
        body.add("file", filePart1);
        body.add("file", filePart2);

        webTestClient.post().uri("/files")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Files uploaded successfully");
    }

    @Test
    @DisplayName("should list files")
    void shouldListFiles() throws Exception {
        var path1 = tempDir.resolve(tempDir + "/testList1.txt");
        Files.write(path1, "test1".getBytes());

        var path2 = tempDir.resolve(tempDir + "/testList2.txt");
        Files.write(path2, "test2".getBytes());

        webTestClient.get().uri("/files")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String[].class)
                .value(files -> {
                    assertThat(files).isNotEmpty();
                    assertThat(files).containsExactlyInAnyOrder("testList1.txt", "testList2.txt");
                });
    }

    @Test
    @DisplayName("should download file")
    void shouldDownloadFile() throws Exception {
        var filename = "testfile.txt";
        var path = tempDir.resolve(tempDir + "/" + filename);
        Files.write(path, "test".getBytes());

        webTestClient.get().uri("/files/{filename}", filename)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .expectHeader().valueEquals(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
    }

    @Test
    @DisplayName("should return bad request when upload an empty file")
    void shouldReturnBadRequestWhenUploadAnEmptyFile() throws IOException {
        var mockMultipartFile = new MockMultipartFile(
                "file",
                "empty.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "".getBytes()
        );

        MultiValueMap<String, HttpEntity<?>> body = new LinkedMultiValueMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<Resource> filePart = new HttpEntity<>(new ByteArrayResource(mockMultipartFile.getBytes()), headers);
        body.add("file", filePart);

        webTestClient.post().uri("/file")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(body)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("Cannot upload empty file.");
    }
}