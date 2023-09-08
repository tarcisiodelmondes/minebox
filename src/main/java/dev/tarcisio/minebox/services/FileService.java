package dev.tarcisio.minebox.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dev.tarcisio.minebox.entities.File;
import dev.tarcisio.minebox.entities.User;
import dev.tarcisio.minebox.exception.FileEmptyException;
import dev.tarcisio.minebox.exception.FileUploadException;
import dev.tarcisio.minebox.payload.response.FileUploadResponse;
import dev.tarcisio.minebox.repositories.FileRepository;
import dev.tarcisio.minebox.utils.S3Utils;
import jakarta.persistence.EntityManager;

@Service
public class FileService {

  @Autowired
  private FileRepository fileRepository;

  @Autowired
  private EntityManager entityManager;

  @Autowired
  private S3Utils s3Utils;

  public List<FileUploadResponse> upload(List<MultipartFile> files)
      throws IOException, FileUploadException {
    List<FileUploadResponse> filesUploaded = new ArrayList<>();

    for (MultipartFile file : files) {
      if (file.isEmpty()) {
        throw new FileEmptyException("O arquivo esta vazio!");
      }

      String fileName = file.getOriginalFilename();
      Long fileSize = file.getSize();
      String fileContentType = file.getContentType();

      if (fileName.indexOf(".") > 0) {
        fileName = fileName.substring(0, fileName.lastIndexOf("."));
      }

      byte[] fileBytes = file.getBytes();
      InputStream fileInputStream = new ByteArrayInputStream(fileBytes);

      String s3FileKey = generateS3FileKey();

      // Upload file in S3
      String s3_url = s3Utils.upload(fileBytes, fileInputStream, s3FileKey);

      // Recupera o usuario autenticado
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      Object principal = authentication.getPrincipal();
      UserDetailsImpl userDetails = (UserDetailsImpl) principal;
      User user = entityManager.getReference(User.class, userDetails.getId());

      File newFile = File.build(fileName, fileSize, fileContentType, s3FileKey, user);
      File fileSaved = fileRepository.save(newFile);

      FileUploadResponse fileUploadResponse = new FileUploadResponse(fileSaved.getName(), fileSaved.getSize(),
          fileSaved.getContentType(),
          s3_url);
      filesUploaded.add(fileUploadResponse);

    }

    return filesUploaded;
  }

  public String generateS3FileKey() {
    return UUID.randomUUID().toString();
  }

}
