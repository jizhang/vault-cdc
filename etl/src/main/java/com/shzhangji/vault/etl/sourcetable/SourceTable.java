package com.shzhangji.vault.etl.sourcetable;

import lombok.Value;

@Value
public class SourceTable {
  int instanceId;
  String database;
  String table;
}
