package dev.tarcisio.minebox.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "files")
public class File extends FileAuditable {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, name = "s3_file_key")
  private String s3FileKey;

  @Column(nullable = false)
  private Long size;

  @Column(nullable = false, name = "content_type")
  private String contentType;

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  @ManyToOne
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  private User user;

  public File() {
  }

  public File(String id, String name, Long size, String contentType, String s3FileKey, User user) {
    this.id = id;
    this.name = name;
    this.size = size;
    this.contentType = contentType;
    this.s3FileKey = s3FileKey;
    this.user = user;
  }

  public static File build(String name, Long size, String contentType, String s3FileKey, User user) {
    File file = new File();
    file.setName(name);
    file.setSize(size);
    file.setContentType(contentType);
    file.setS3FileKey(s3FileKey);
    file.setUser(user);

    return file;
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

  public String getS3FileKey() {
    return s3FileKey;
  }

  public void setS3FileKey(String s3FileKey) {
    this.s3FileKey = s3FileKey;
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

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

}
