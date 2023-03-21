package com.qs.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({"column_name", "data_type"})
public class ColumnMetaData {
    @JsonProperty("column_name")
    private String columnName;

    @JsonProperty("data_type")
    private String dataType;
}
