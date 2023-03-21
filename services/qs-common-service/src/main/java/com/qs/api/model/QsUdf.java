package com.qs.api.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "QS_Udf")
public class QsUdf {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int udfId;
	
	private int projectId;
	private int userId;
	private String udfName;
    private String udfSyntax;
	private String udfFilepath;
	private String udfVersion;
	private String udfIconpath;
	private String udfScriptlanguage;
	private String arguments;
	
	private boolean active;
	private Date createdDate;
	private Date modifiedDate;
	private String createdBy;
	private String modifiedBy;
	
}
