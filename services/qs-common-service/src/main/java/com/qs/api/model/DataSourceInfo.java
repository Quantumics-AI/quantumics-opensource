package com.qs.api.model;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "qsp_datasources_info")
public class DataSourceInfo {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long dataSourceId;
  
  private int projectId;
  private String connectionName;
  private String type;
  private String host;
  private String port;
  private String schemaName;
  private String userName;
  private String password; // TODO: change this to char[] instead.
  private String createdBy;
  private Date creationDate;
  
}
