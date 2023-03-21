package com.qs.api.request;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"dataSourceId", "dataSourceType", "dataSourceName", "dataSourceImage", "active", "creationDate"})
public class DatasourceTypesInfo {
	private int dataSourceId;
	
	private String dataSourceType;
	private String dataSourceName;
	private String dataSourceImage;
	private boolean active;
	private Date creationDate;
}
