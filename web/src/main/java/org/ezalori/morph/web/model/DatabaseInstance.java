package org.ezalori.morph.web.model;

import java.util.Date;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class DatabaseInstance {
  @Id
  private Integer id;
  private String name;
  private String host;
  private Integer port;
  private String username;
  private String password;
  private Date createdAt;
  private Date updatedAt;
}
