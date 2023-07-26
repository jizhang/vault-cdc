package com.shzhangji.vault.cdc.extractconfig;

import java.util.Date;
import lombok.Data;

@Data
public class ExtractConfigRow {
  private int sourceInstanceId;
  private String sourceDatabase;
  private String sourceTable;
  private boolean isRegex;
  private String primaryKeys;
  private String extractColumns;
  private int targetInstanceId;
  private String targetDatabase;
  private String targetTable;
  private int partitionType;
  private String partitionInputColumn;
  private String partitionOutputColumn;
  private boolean ignoreDelete;
  private Date updatedAt;
}
