package dev.tarcisio.minebox.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.tarcisio.minebox.exception.FileAccessNotAllowed;
import dev.tarcisio.minebox.exception.FileEmptyException;
import dev.tarcisio.minebox.exception.FileNotFoundException;
import dev.tarcisio.minebox.exception.FileUploadException;
import dev.tarcisio.minebox.exception.S3Exception;
import dev.tarcisio.minebox.payload.request.FileRenameRequest;
import dev.tarcisio.minebox.payload.response.FileDownloadResponse;
import dev.tarcisio.minebox.payload.response.FileListResponse;
import dev.tarcisio.minebox.payload.response.FileResponse;
import dev.tarcisio.minebox.payload.response.FileUploadResponse;
import dev.tarcisio.minebox.repositories.FileRepository;
import dev.tarcisio.minebox.services.FileService;

@SpringBootTest
@AutoConfigureMockMvc
public class FileControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private FileService fileService;

  @MockBean
  private FileRepository fileRepository;

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

  @Test
  public void testDownloadShould200WithFileBytes() throws Exception {
    byte[] fileBytes = new byte[1234];

    FileDownloadResponse fileDownloadResponse = new FileDownloadResponse(fileBytes, "image/png", 10L);
    Mockito.when(fileService.download("file_id")).thenReturn(fileDownloadResponse);

    mockMvc
        .perform(
            get("/api/file/download/file_id").with(SecurityMockMvcRequestPostProcessors.user("test@email.com"))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andExpect(content().bytes(fileBytes));
  }

  @Test
  public void testDownloadShould400WithS3ExceptionMessage() throws Exception {

    Mockito.when(fileService.download("file_id")).thenThrow(new S3Exception("Error: falha ao baixar arquivo!"));
    mockMvc
        .perform(
            get("/api/file/download/file_id").with(SecurityMockMvcRequestPostProcessors.user("test@email.com"))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest()).andExpect(content().string("Error: falha ao baixar arquivo!"));
  }

  @Test
  public void testDownloadShould404WithFileNotFoundExceptionMessage() throws Exception {

    Mockito.when(fileService.download("file_id"))
        .thenThrow(new FileNotFoundException("Error: arquivo não encontrado!"));
    mockMvc
        .perform(
            get("/api/file/download/file_id").with(SecurityMockMvcRequestPostProcessors.user("test@email.com"))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound()).andExpect(content().string("Error: arquivo não encontrado!"));
  }

  @Test
  public void testRenameShould200WithFileResponse() throws Exception {
    FileRenameRequest fileRenameRequest = new FileRenameRequest();
    fileRenameRequest.setName("new_name");

    FileResponse fileResponse = new FileResponse("id", "new_name", 10L, "image/png");
    Mockito.when(fileService.rename(Mockito.any(), Mockito.any(FileRenameRequest.class))).thenReturn(fileResponse);

    mockMvc
        .perform(
            put("/api/file/rename/file_id").with(SecurityMockMvcRequestPostProcessors.user("test@email.com"))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(fileRenameRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("new_name")).andExpect(jsonPath("$.id").value("id"))
        .andExpect(jsonPath("$.size").value(10L)).andExpect(jsonPath("$.contentType").value("image/png"));
  }

  @Test
  public void testRenameShould404WithFileNotFoundException() throws Exception {
    FileRenameRequest fileRenameRequest = new FileRenameRequest();
    fileRenameRequest.setName("new_name");

    Mockito.when(fileService.rename(Mockito.any(), Mockito.any(FileRenameRequest.class)))
        .thenThrow(new FileNotFoundException("Error: arquivo não encontrado!"));

    mockMvc
        .perform(
            put("/api/file/rename/file_id").with(SecurityMockMvcRequestPostProcessors.user("test@email.com"))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(fileRenameRequest)))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Error: arquivo não encontrado!"));
  }

  @Test
  public void testRenameShould403WithFileAccessNotAllowed() throws Exception {
    FileRenameRequest fileRenameRequest = new FileRenameRequest();
    fileRenameRequest.setName("new_name");

    Mockito.when(fileService.rename(Mockito.any(), Mockito.any(FileRenameRequest.class)))
        .thenThrow(new FileAccessNotAllowed("Error: você não tem permisão para renomear esse arquivo!"));

    mockMvc
        .perform(
            put("/api/file/rename/file_id").with(SecurityMockMvcRequestPostProcessors.user("test@email.com"))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(fileRenameRequest)))
        .andExpect(status().isForbidden())
        .andExpect(content().string("Error: você não tem permisão para renomear esse arquivo!"));
  }

  @Test
  public void testDeleteShould200WithPlainText() throws Exception {
    Mockito.doNothing().when(fileService).delete(Mockito.any());

    mockMvc
        .perform(
            delete("/api/file/delete/file_id").with(SecurityMockMvcRequestPostProcessors.user("test@email.com"))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string("Arquivo deletado com sucesso!"));

  }

  @Test
  public void testDeleteShould404WithFileNotFoundException() throws Exception {
    Mockito.doThrow(new FileNotFoundException("Error: arquivo não encontrado!")).when(fileService)
        .delete(Mockito.any());

    mockMvc
        .perform(
            delete("/api/file/delete/file_id").with(SecurityMockMvcRequestPostProcessors.user("test@email.com"))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Error: arquivo não encontrado!"));
  }

  @Test
  public void testDeleteShould403WithFileAccessNotAllowed() throws Exception {
    Mockito.doThrow(new FileAccessNotAllowed("Error: você não tem permisão para renomear esse arquivo!"))
        .when(fileService)
        .delete(Mockito.any());

    mockMvc
        .perform(
            delete("/api/file/delete/file_id").with(SecurityMockMvcRequestPostProcessors.user("test@email.com"))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden())
        .andExpect(content().string("Error: você não tem permisão para renomear esse arquivo!"));
  }

}
