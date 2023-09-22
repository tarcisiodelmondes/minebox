package dev.tarcisio.minebox.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import dev.tarcisio.minebox.entities.File;
import dev.tarcisio.minebox.entities.User;
import dev.tarcisio.minebox.exception.FileAccessNotAllowed;
import dev.tarcisio.minebox.exception.FileEmptyException;
import dev.tarcisio.minebox.exception.FileNotFoundException;
import dev.tarcisio.minebox.payload.request.FileRenameRequest;
import dev.tarcisio.minebox.payload.response.FileDownloadResponse;
import dev.tarcisio.minebox.payload.response.FileResponse;
import dev.tarcisio.minebox.payload.response.FileUploadResponse;
import dev.tarcisio.minebox.repositories.FileRepository;
import dev.tarcisio.minebox.utils.S3Utils;
import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {

  @Mock
  private FileRepository fileRepository;

  @Mock
  private S3Utils s3Utils;

  @Mock
  private EntityManager entityManager;

  @InjectMocks
  private FileService fileService;

  @Test
  public void whenUploadShouldReturnListOfFileUploadResponse() throws IOException {
    User user = new User("Fulano", "fulano@email.com", "12345678");
    File newFile = File.build("fake_file", 10L, "image/png", "s3_key", user);

    Mockito.when(s3Utils.upload(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn("s3_url");
    Mockito.when(fileRepository.save(Mockito.any())).thenReturn(newFile);
    Mockito.when(entityManager.getReference(User.class, "id")).thenReturn(user);

    UserDetailsImpl userDetails = new UserDetailsImpl("id", "Fulano", "fulano@email.com", "12345678");
    Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
        userDetails.getAuthorities());

    SecurityContextHolder.getContext().setAuthentication(authentication);

    MockMultipartFile createFile = new MockMultipartFile("file", "fake_file", MediaType.IMAGE_PNG_VALUE,
        "fake_image".getBytes());
    MultipartFile file = (MultipartFile) createFile;
    List<MultipartFile> fileList = new ArrayList<>();
    fileList.add(file);
    List<FileUploadResponse> result = fileService.upload(fileList);
    FileUploadResponse firstItemOfResult = result.get(0);

    assertEquals("s3_url", firstItemOfResult.getS3_url());
    assertEquals(FileUploadResponse.class, firstItemOfResult.getClass());

  }

  @Test
  public void whenUploadShouldReturnFileEmptyException() throws FileEmptyException {
    MockMultipartFile createFile = new MockMultipartFile("file", "fake_file", MediaType.IMAGE_PNG_VALUE,
        new byte[0]);
    MultipartFile file = (MultipartFile) createFile;

    List<MultipartFile> fileList = new ArrayList<>();
    fileList.add(file);

    assertThrows(FileEmptyException.class, () -> fileService.upload(fileList));

  }

  @Test
  public void whenDownloadShouldReturnFileDownloadResponse() throws Exception {
    User user = new User("Fulano", "test@email.com", "12345678");
    File file = new File("id", "file_name", 10L, "image/png", "s3_file_key", user);

    Mockito.doReturn(Optional.of(file)).when(fileRepository).findById("id");

    byte[] fileBytes = new byte[1234];
    Mockito.when(s3Utils.getFileBytes("s3_file_key")).thenReturn(fileBytes);

    FileDownloadResponse result = fileService.download(file.getId());

    assertEquals(FileDownloadResponse.class, result.getClass());
    assertEquals(fileBytes, result.getFilebytes());
    assertEquals(file.getSize(), result.getSize());
    assertEquals(file.getContentType(), result.getContentType());

  }

  @Test
  public void whenDownloadShouldReturnFileNotFoundException() throws Exception {
    assertThrows(FileNotFoundException.class, () -> fileService.download(Mockito.anyString()));
  }

  @Test
  public void whenRenameShouldReturnFileResponse() throws Exception {
    User user = new User("Fulano", "test@email.com", "12345678");
    user.setId("id");
    File file = new File("id", "file_name", 10L, "image/png", "s3_file_key", user);

    // Mock contexto de autenticação
    UserDetailsImpl userDetails = new UserDetailsImpl("id", "Fulano", "test@email.com", "12345678");
    Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
        userDetails.getAuthorities());

    SecurityContextHolder.getContext().setAuthentication(authentication);

    Mockito.when(fileRepository.findById(Mockito.any())).thenReturn(Optional.of(file));
    Mockito.when(fileRepository.save(Mockito.any())).thenReturn(file);

    FileRenameRequest fileRenameRequest = new FileRenameRequest();
    fileRenameRequest.setName("novo_nome");

    FileResponse result = fileService.rename("id", fileRenameRequest);

    assertEquals("novo_nome", result.getName());
    assertEquals("id", result.getId());
    assertEquals(10L, result.getSize());
    assertEquals("image/png", result.getContentType());

  }

  @Test
  public void whenRenameShouldReturnFileNotFoundException() throws Exception {
    FileRenameRequest fileRenameRequest = new FileRenameRequest();
    fileRenameRequest.setName("novo_nome");
    assertThrows(FileNotFoundException.class, () -> fileService.rename("id", fileRenameRequest));
  }

  @Test
  public void whenRenameShouldReturnFileAccessNotAllowed() throws Exception {
    User user = new User("Fulano", "test@email.com", "12345678");
    user.setId("id");
    File file = new File("id", "file_name", 10L, "image/png", "s3_file_key", user);

    // Mock contexto de autenticação
    UserDetailsImpl userDetails = new UserDetailsImpl("id_diferente", "Fulano", "test@email.com", "12345678");
    Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
        userDetails.getAuthorities());

    SecurityContextHolder.getContext().setAuthentication(authentication);

    Mockito.when(fileRepository.findById(Mockito.any())).thenReturn(Optional.of(file));

    FileRenameRequest fileRenameRequest = new FileRenameRequest();
    fileRenameRequest.setName("novo_nome");

    assertThrows(FileAccessNotAllowed.class, () -> fileService.rename("id", fileRenameRequest));

  }

  @Test
  public void whenDeleteShouldReturnVoid() throws Exception {
    User user = new User("Fulano", "test@email.com", "12345678");
    user.setId("id");
    File file = new File("id", "file_name", 10L, "image/png", "s3_file_key", user);

    // Mock contexto de autenticação
    UserDetailsImpl userDetails = new UserDetailsImpl("id", "Fulano", "test@email.com", "12345678");
    Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
        userDetails.getAuthorities());

    SecurityContextHolder.getContext().setAuthentication(authentication);

    Mockito.when(fileRepository.findById(Mockito.any())).thenReturn(Optional.of(file));
    Mockito.doNothing().when(fileRepository).deleteById(Mockito.any());
    Mockito.doNothing().when(s3Utils).deleteFile(Mockito.any());

    fileService.delete("id");

    Mockito.verify(fileRepository, Mockito.times(1)).findById("id");
    Mockito.verify(fileRepository, Mockito.times(1)).deleteById("id");
    Mockito.verify(s3Utils, Mockito.times(1)).deleteFile("s3_file_key");

  }

  @Test
  public void whenDeleteShouldReturnFileNotFoundException() throws Exception {
    assertThrows(FileNotFoundException.class, () -> fileService.delete("id"));

    Mockito.verify(fileRepository, Mockito.times(1)).findById("id");
    Mockito.verify(fileRepository, Mockito.times(0)).deleteById("id");
    Mockito.verify(s3Utils, Mockito.times(0)).deleteFile("s3_file_key");
  }

  @Test
  public void whenDeleteShouldReturnFileAccessNotAllowed() throws Exception {
    User user = new User("Fulano", "test@email.com", "12345678");
    user.setId("id");
    File file = new File("id", "file_name", 10L, "image/png", "s3_file_key", user);

    // Mock contexto de autenticação
    UserDetailsImpl userDetails = new UserDetailsImpl("id_diferente", "Fulano", "test@email.com", "12345678");
    Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
        userDetails.getAuthorities());

    SecurityContextHolder.getContext().setAuthentication(authentication);

    Mockito.when(fileRepository.findById(Mockito.any())).thenReturn(Optional.of(file));

    assertThrows(FileAccessNotAllowed.class, () -> fileService.delete("id"));

    Mockito.verify(fileRepository, Mockito.times(1)).findById("id");
    Mockito.verify(fileRepository, Mockito.times(0)).deleteById("id");
    Mockito.verify(s3Utils, Mockito.times(0)).deleteFile("s3_file_key");

  }

}
