package com.qs.api.request;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"udfId", "projectId", "udfName", "udfInfo", "arguments", "active", "createdDate","modifiedDate","createdBy","modifiedBy"})
public class UdfInfo {
	private int udfId;
	private int projectId;
	
	private String udfName;
	private String udfInfo;
	private String arguments;
	private boolean active;
	
	private Date createdDate;
	private Date modifiedDate;
	private String createdBy;
	private String modifiedBy;
}
