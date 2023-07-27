package com.shzhangji.vault.cdc.targettable;

import com.shzhangji.vault.cdc.sourcetable.RowKind;
import java.util.List;
import java.util.Map;
import lombok.Value;

@Value
public class TargetRow {
  int instanceId;
  String database;
  String table;
  List<String> primaryKeys;
  Map<String, String> columns;
  RowKind rowKind;
}
