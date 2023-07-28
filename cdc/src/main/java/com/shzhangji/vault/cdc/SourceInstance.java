package com.shzhangji.vault.cdc;

import java.util.List;
import lombok.Value;

@Value
public class SourceInstance {
  int instanceId;
  List<String> databaseList;
  List<String> tableList;
}
