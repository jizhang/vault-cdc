package com.shzhangji.vault.etl.sourcetable;

import java.util.Map;

public class EmptyExtractor implements PartitionExtractor {
  @Override
  public Map<String, String> extract(SourceTable sourceTable, Map<String, String> columns) {
    return Map.of();
  }
}
