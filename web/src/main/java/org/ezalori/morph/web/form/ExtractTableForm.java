package org.ezalori.morph.web.form;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class ExtractTableForm {
  private Integer id;
  @NotNull
  private Integer sourceInstance;
  @NotBlank
  private String sourceDatabase;
  @NotBlank
  private String sourceTable;
  @NotNull
  private Integer targetInstance;
  @NotBlank
  private String targetDatabase;
  @NotBlank
  private String targetTable;
  @NotNull
  private String columnList;
}
