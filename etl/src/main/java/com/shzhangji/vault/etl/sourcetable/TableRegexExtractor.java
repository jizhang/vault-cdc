package com.shzhangji.vault.etl.sourcetable;

import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TableRegexExtractor implements PartitionExtractor {
  private final Pattern tableRegex;
  private final String outputColumn;

  @Override
  public Map<String, String> extract(SourceTable sourceTable, Map<String, String> columns) {
    var matcher = tableRegex.matcher(sourceTable.getTable());
    if (matcher.matches()) {
      return Map.of(outputColumn, matcher.group(1));
    }

    throw new RuntimeException(String.format(
      "Fail to extract partition key. Table name is '%s', regex is '%s'.",
      sourceTable.getTable(), tableRegex));
  }
}
