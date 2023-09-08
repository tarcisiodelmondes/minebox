package dev.tarcisio.minebox.payload.response;

public class FileUploadResponse {
  private String name;
  private Long size;
  private String contentType;
  private String s3_url;

  public FileUploadResponse() {
  }

  public FileUploadResponse(String name, Long size, String contentType, String s3_url) {
    this.name = name;
    this.size = size;
    this.contentType = contentType;
    this.s3_url = s3_url;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getSize() {
    return size;
  }

  public void setSize(Long size) {
    this.size = size;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public String getS3_url() {
    return s3_url;
  }

  public void setS3_url(String s3_url) {
    this.s3_url = s3_url;
  }
}
