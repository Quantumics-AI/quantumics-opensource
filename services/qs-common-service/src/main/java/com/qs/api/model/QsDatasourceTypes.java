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
@Table(name = "QS_Datasource_Types")
public class QsDatasourceTypes extends MetaData{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int dataSourceId;
	
	private String dataSourceType;
	private String dataSourceName;
	private String dataSourceImage;
	private boolean active;
	private Date creationDate;
}
