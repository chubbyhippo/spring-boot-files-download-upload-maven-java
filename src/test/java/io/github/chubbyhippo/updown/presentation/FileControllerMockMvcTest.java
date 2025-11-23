package io.github.chubbyhippo.updown.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.chubbyhippo.updown.application.FileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileController.class)
class FileControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private FileService fileService;
    @Autowired
    private ObjectMapper jacksonObjectMapper;

    @Test
    @DisplayName("should upload a file")
    void shouldUploadAFile() throws Exception {

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
        when(fileService.listFiles()).thenReturn(Stream.of("testList1.txt", "testList2.txt"));

        var mvcResult = mockMvc.perform(get("/files")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        var json = mvcResult.getResponse().getContentAsString();
        var contentAsString = jacksonObjectMapper.readValue(json, String[].class);
        var sorted = Arrays.stream(contentAsString).sorted().toArray();

        assertThat(sorted).isEqualTo(new String[]{"testList1.txt", "testList2.txt"});

    }

    @Test
    @DisplayName("should download file")
    void shouldDownloadFile() throws Exception {

        var filename = "testfile.txt";
        var mockResource = mock(Resource.class);

        when(mockResource.getFilename()).thenReturn(filename);
        when(fileService.loadAsResource(filename)).thenReturn(mockResource);

        mockMvc.perform(get("/files/{filename}", filename))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename*=UTF-8''" +   filename ));
    }

}