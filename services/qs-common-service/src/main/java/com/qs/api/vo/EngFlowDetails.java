package com.qs.api.vo;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({
  "category",
  "type",
  "external"
})
public class EngFlowDetails {
  private String type;
  private String category;
  private boolean external;
  List<FileDetails> fileDetails = new ArrayList<>();
}
