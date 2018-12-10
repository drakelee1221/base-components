
package com.base.components.common.dto.io;

import java.io.Serializable;

/**
 * 用于feign的文件传输对象，最终生成的url为：path / fileName
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-08-02 18:28
 */
public class FileDTO implements Serializable {
  private static final long serialVersionUID = 1234330971510656405L;

  private String fileName;

  private String path;

  private String userId;

  private byte[] uploadBytes;

  private Boolean privateFile = Boolean.TRUE;

  private Boolean tempFile = Boolean.FALSE;

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public byte[] getUploadBytes() {
    return uploadBytes;
  }

  public void setUploadBytes(byte[] uploadBytes) {
    this.uploadBytes = uploadBytes;
  }

  public Boolean getPrivateFile() {
    return privateFile;
  }

  public void setPrivateFile(Boolean privateFile) {
    this.privateFile = privateFile;
  }

  public Boolean getTempFile() {
    return tempFile;
  }

  public void setTempFile(Boolean tempFile) {
    this.tempFile = tempFile;
  }
}
