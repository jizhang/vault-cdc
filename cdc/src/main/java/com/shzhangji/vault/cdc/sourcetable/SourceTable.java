package com.shzhangji.vault.cdc.sourcetable;

import lombok.Value;

@Value
public class SourceTable {
  int instanceId;
  String database;
  String table;
}
