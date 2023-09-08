package dev.tarcisio.minebox.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import dev.tarcisio.minebox.exception.FileEmptyException;
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

}
