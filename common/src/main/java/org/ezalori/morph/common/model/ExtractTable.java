package org.ezalori.morph.common.model;

import java.util.Date;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class ExtractTable {
  @Id
  private Integer id;
  private Integer sourceInstance;
  private String sourceDatabase;
  private String sourceTable;
  private Integer targetInstance;
  private String targetDatabase;
  private String targetTable;
  private String columnList;
  private Date createdAt;
  private Date updatedAt;
}
