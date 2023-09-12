package dev.tarcisio.minebox.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dev.tarcisio.minebox.entities.File;
import dev.tarcisio.minebox.entities.User;
import dev.tarcisio.minebox.exception.FileEmptyException;
import dev.tarcisio.minebox.exception.FileNotFoundException;
import dev.tarcisio.minebox.exception.FileUploadException;
import dev.tarcisio.minebox.exception.S3Exception;
import dev.tarcisio.minebox.payload.response.FileDownloadResponse;
import dev.tarcisio.minebox.payload.response.FileListResponse;
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

      String s3FileKey = UUID.randomUUID().toString();
      ;

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

  public List<FileListResponse> list() {
    // Pegar o id do usuario logogado
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();

    // Pega todos os arquvios do usuario
    List<FileListResponse> filesOfUser = fileRepository.findAllByUserId(principal.getUsername()).stream()
        .map(file -> {
          // Gera as urls pre assinada para cada arquivo
          String s3Url = s3Utils.generateUrlPreAssinada(file.getS3FileKey());

          return new FileListResponse(s3Url, file.getId(), file.getName(), file.getSize(), file.getContentType());
        })
        .toList();

    return filesOfUser;
  }

  public FileDownloadResponse download(String fileId) throws S3Exception, FileNotFoundException {
    Optional<File> file = fileRepository.findById(fileId);

    if (!file.isPresent()) {
      throw new FileNotFoundException("Error: arquivo não encontrado!");
    }

    File fileObject = file.get();

    byte[] fileBytes = s3Utils.getFileBytes(fileObject.getS3FileKey());

    FileDownloadResponse fileDownloadResponse = new FileDownloadResponse(fileBytes, fileObject.getContentType(),
        fileObject.getSize());
    return fileDownloadResponse;

  }

}
