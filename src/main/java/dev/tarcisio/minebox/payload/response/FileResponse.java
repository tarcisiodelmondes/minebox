package dev.tarcisio.minebox.payload.response;

public class FileResponse {
  private String id;
  private String name;
  private Long size;
  private String contentType;

  public FileResponse(String id, String name, Long size, String contentType) {
    this.id = id;
    this.name = name;
    this.size = size;
    this.contentType = contentType;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

}
