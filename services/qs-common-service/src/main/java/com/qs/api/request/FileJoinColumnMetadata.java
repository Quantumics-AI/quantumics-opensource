package com.qs.api.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
public class FileJoinColumnMetadata {
  @JsonProperty("eventId") private int eventId;
  @JsonProperty("metadata") private List<Metadata> metadata;
}
