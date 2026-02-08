package io.github.chubbyhippo.updown.restdocs;

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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseBody;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Execution(ExecutionMode.SAME_THREAD)
@AutoConfigureMockMvc
class UpDownApplicationMockMvcIntegrationRestdocsTests {

    @Autowired
    private MockMvc mockMvc;
    @TempDir
    private static Path tempDir;
    @Autowired
    private JsonMapper jsonMapper;
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
    @DisplayName("test upload file")
    void testUploadFile() throws Exception {
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
                .andExpect(content().string("File uploaded successfully: hello.txt"))
                .andDo(document("upload-file",
                        requestParts(
                                partWithName("file").description("The file to upload")
                        )));

    }

    @Test
    @DisplayName("test upload files")
    void testUploadFiles() throws Exception {
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
                .andExpect(content().string("Files uploaded successfully"))
                .andDo(document("upload-files",
                        requestParts(
                                partWithName("file").description("Files to upload")
                        ),
                        responseBody()));

    }

    @Test
    @DisplayName("test list files")
    void testListFiles() throws Exception {
        var path1 = tempDir.resolve("testList1.txt");
        Files.write(path1, "test1".getBytes());

        var path2 = tempDir.resolve("testList2.txt");
        Files.write(path2, "test2".getBytes());

        var mvcResult = mockMvc.perform(get("/files")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("list-files",
                        responseFields(
                                fieldWithPath("[]").description("Array of filenames")
                        )))
                .andReturn();

        var json = mvcResult.getResponse().getContentAsString();
        var files = jsonMapper.readValue(json, String[].class);

        assertThat(files).isNotEmpty()
                .contains("testList1.txt", "testList2.txt");

    }

    @Test
    @DisplayName("test download file")
    void testDownloadFile() throws Exception {
        var filename = "testfile.txt";
        var path = tempDir.resolve(filename);
        Files.write(path, "test".getBytes());

        mockMvc.perform(get("/files/{filename}", filename))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename*=UTF-8''" + filename));
    }

    @Test
    @DisplayName("test return bad request when uploading an empty file")
    void testReturnBadRequestWhenUploadingAnEmptyFile() throws Exception {
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
                .andExpect(content().string("Cannot upload empty file."))
                .andDo(document("upload-empty-file-error",
                        responseBody()));
    }

    @Test
    @DisplayName("test download zip")
    void testDownloadZip() throws Exception {
        var filenames = List.of("test1.txt", "test2.txt");

        var path1 = tempDir.resolve(filenames.getFirst());
        Files.write(path1, "test1".getBytes());
        var path2 = tempDir.resolve(filenames.getLast());
        Files.write(path2, "test2".getBytes());

        var jsonFilenames = jsonMapper.writeValueAsString(filenames);
        System.out.println(jsonFilenames);
        mockMvc.perform(post("/zip")
                        .content(jsonFilenames)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"files.zip\""))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andDo(document("download-zip",
                        requestFields(
                                fieldWithPath("[]").description("Array of filenames to include in the zip")
                        ),
                        responseHeaders(
                                headerWithName("Content-Disposition").description("Attachment header with zip filename"),
                                headerWithName("Content-Type").description("Content type of the response")
                        )));
    }
}
