package com.qs.api.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({
  "fileId",
  "fileName",
  "folderId",
  "category",
  "createdDate"
})
public class FileDetails {
  private int fileId;
  private String fileName;
  private int folderId;
  private String category;
  private Date createdDate; 
}
