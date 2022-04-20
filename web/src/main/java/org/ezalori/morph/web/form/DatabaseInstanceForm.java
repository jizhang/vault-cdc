package org.ezalori.morph.web.form;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class DatabaseInstanceForm {
  private Integer id;
  @NotBlank
  private String name;
  @NotBlank
  private String host;
  @NotNull
  private Integer port;
  @NotBlank
  private String username;
  @NotNull
  private String password;
}
