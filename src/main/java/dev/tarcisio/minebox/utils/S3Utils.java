package dev.tarcisio.minebox.utils;

import java.io.InputStream;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import dev.tarcisio.minebox.exception.FileUploadException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Component
public class S3Utils {

  @Autowired
  private S3Client s3Client;

  @Value("${aws.bucket.name}")
  private String bucketName;

  public String upload(byte[] fileBytes, InputStream fileInputStream, String s3FileKey) throws FileUploadException {
    try {
      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(bucketName)
          .key(s3FileKey).build();

      s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(fileInputStream, fileBytes.length));

      GetObjectRequest getObjectRequest = GetObjectRequest.builder()
          .bucket(bucketName)
          .key(s3FileKey)
          .build();

      GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
          .signatureDuration(Duration.ofMinutes(120)) // 2 horas
          .getObjectRequest(getObjectRequest)
          .build();

      S3Presigner presigner = S3Presigner.create();
      PresignedGetObjectRequest presignedGetObjectRequest = presigner.presignGetObject(getObjectPresignRequest);

      return presignedGetObjectRequest.url().toString();
    } catch (Exception e) {
      throw new FileUploadException("Error ao fazer envio do arquivo!");
    }

  }
}
