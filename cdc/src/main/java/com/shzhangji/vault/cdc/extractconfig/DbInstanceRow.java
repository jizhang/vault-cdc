package com.shzhangji.vault.cdc.extractconfig;

import lombok.Data;

@Data
public class DbInstanceRow {
  private int id;
  private String host;
  private int port;
  private String username;
  private String password;
  private String database;
}
