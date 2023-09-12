package dev.tarcisio.minebox.payload.response;

public class FileDownloadResponse {
  private byte[] filebytes;
  private String contentType;
  private Long size;

  public FileDownloadResponse(byte[] filebytes, String contentType, Long size) {
    this.filebytes = filebytes;
    this.contentType = contentType;
    this.size = size;
  }

  public byte[] getFilebytes() {
    return filebytes;
  }

  public void setFilebytes(byte[] filebytes) {
    this.filebytes = filebytes;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public Long getSize() {
    return size;
  }

  public void setSize(Long size) {
    this.size = size;
  }

}
