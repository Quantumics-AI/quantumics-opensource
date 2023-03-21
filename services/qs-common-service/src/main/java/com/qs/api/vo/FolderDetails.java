package com.qs.api.vo;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({
  "category",
  "type",
  "folderId",
  "name",
  "external"
})
public class FolderDetails {
  private String type = "folder";
  private String name;
  private int folderId;
  private String category;
  private boolean external;
  
  List<FileDetails> fileDetails = new ArrayList<>();
}
