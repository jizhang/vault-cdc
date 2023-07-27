package com.shzhangji.vault.cdc.targettable;

import lombok.Value;

@Value
public class TargetTable {
  String database;
  String table;
}
