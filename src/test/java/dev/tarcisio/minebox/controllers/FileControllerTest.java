package dev.tarcisio.minebox.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import dev.tarcisio.minebox.exception.FileEmptyException;
import dev.tarcisio.minebox.exception.FileUploadException;
import dev.tarcisio.minebox.payload.response.FileListResponse;
import dev.tarcisio.minebox.payload.response.FileUploadResponse;
import dev.tarcisio.minebox.services.FileService;

@SpringBootTest
@AutoConfigureMockMvc
public class FileControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private FileService fileService;

  @Test
  public void testUploadShould201WithListOfFileUploadResponse() throws IOException, FileUploadException, Exception {
    FileUploadResponse fileUploadResponse = new FileUploadResponse("Fulano", 1000L, "image/png", "s3_url");
    List<FileUploadResponse> result = new ArrayList<>();
    result.add(fileUploadResponse);

    Mockito.when(fileService.upload(Mockito.any())).thenReturn(result);

    MockMultipartFile file = new MockMultipartFile("file", "fake_file.txt", MediaType.TEXT_PLAIN_VALUE,
        "fake".getBytes());
    mockMvc
        .perform(
            multipart("/api/file/upload").file(file).with(SecurityMockMvcRequestPostProcessors.user("test@email.com"))
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isCreated()).andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].name").value("Fulano"));
  }

  @Test
  public void testUploadShould400WithFileEmptyException()
      throws IOException, FileUploadException, FileEmptyException, Exception {

    MockMultipartFile file = new MockMultipartFile("file", "fake_file.txt", MediaType.IMAGE_PNG_VALUE,
        "image".getBytes());

    Mockito.when(fileService.upload(Mockito.any())).thenThrow(new FileEmptyException("O arquivo está vazio!"));

    mockMvc
        .perform(
            multipart("/api/file/upload").file(file).with(SecurityMockMvcRequestPostProcessors.user("test@email.com"))
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isBadRequest()).andExpect(content().string("O arquivo está vazio!"));

  }

  @Test
  public void testUploadShould400WithFileUploadException()
      throws IOException, FileUploadException, Exception {

    MockMultipartFile file = new MockMultipartFile("file", "fake_file.txt", MediaType.IMAGE_PNG_VALUE,
        "image".getBytes());

    Mockito.when(fileService.upload(Mockito.any())).thenThrow(new FileUploadException("Error ao enviar arquivo!"));

    mockMvc
        .perform(
            multipart("/api/file/upload").file(file).with(SecurityMockMvcRequestPostProcessors.user("test@email.com"))
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isBadRequest()).andExpect(content().string("Error ao enviar arquivo!"));

  }

  @Test
  public void testListShould200WithFileListResponse() throws Exception {
    FileListResponse fileListResponse = new FileListResponse("s3_url", "id", "nome do arquivo", 2333L, "image/png");
    Mockito.when(fileService.list()).thenReturn(List.of(fileListResponse));

    mockMvc
        .perform(
            get("/api/file/list").with(SecurityMockMvcRequestPostProcessors.user("test@email.com"))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].name").value("nome do arquivo"));
  }

  @Test
  public void testListShould200WithArrayEmpty() throws Exception {
    Mockito.when(fileService.list()).thenReturn(new ArrayList<>());

    mockMvc
        .perform(
            get("/api/file/list").with(SecurityMockMvcRequestPostProcessors.user("test@email.com"))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").value(new ArrayList<>()));
  }

}
