package io.github.chubbyhippo.updown;

import io.github.chubbyhippo.updown.domain.StorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Execution(ExecutionMode.SAME_THREAD)
class UpDownApplicationWebTestClientIntegrationTests {

    @Autowired
    private WebTestClient webTestClient;
    @TempDir
    private static Path tempDir;
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
    void shouldUploadFiles() {

        var multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("file", "test1".getBytes())
                .contentType(MediaType.TEXT_PLAIN)
                .filename("file1.txt");
        multipartBodyBuilder.part("file", "test2".getBytes())
                .contentType(MediaType.TEXT_PLAIN)
                .filename("file2.txt");


        webTestClient.post().uri("/files")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Files uploaded successfully");
    }

    @Test
    @DisplayName("should list files")
    void shouldListFiles() throws Exception {
        var path1 = tempDir.resolve("testList1.txt");
        Files.write(path1, "test1".getBytes());

        var path2 = tempDir.resolve("testList2.txt");
        Files.write(path2, "test2".getBytes());

        webTestClient.get().uri("/files")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String[].class)
                .value(files -> {
                    assertThat(files).isNotEmpty();
                    assertThat(files).contains("testList1.txt", "testList2.txt");
                });
    }

    @Test
    @DisplayName("should download file")
    void shouldDownloadFile() throws Exception {
        var filename = "testfile.txt";
        var path = tempDir.resolve(filename);
        Files.write(path, "test".getBytes());

        webTestClient.get().uri("/files/{filename}", filename)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .expectHeader().valueEquals(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename);
    }

    @Test
    @DisplayName("should return bad request when upload an empty file")
    void shouldReturnBadRequestWhenUploadAnEmptyFile() {
        var filename = "hello.txt";
        var multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("file", "".getBytes())
                .contentType(MediaType.TEXT_PLAIN)
                .filename(filename);

        webTestClient.post().uri("/file")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("Cannot upload empty file.");
    }
}