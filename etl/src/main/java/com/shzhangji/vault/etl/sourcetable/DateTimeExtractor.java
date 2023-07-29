package com.shzhangji.vault.etl.sourcetable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DateTimeExtractor implements PartitionExtractor {
  private static final DateTimeFormatter INPUT_PATTERN =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final DateTimeFormatter OUTPUT_PATTERN = DateTimeFormatter.ofPattern("yyyyMMdd");

  private final String inputColumn;
  private final String outputColumn;

  @Override
  public Map<String, String> extract(SourceTable sourceTable, Map<String, String> columns) {
    var dt = LocalDateTime.parse(columns.get(inputColumn), INPUT_PATTERN);
    return Map.of(outputColumn, dt.format(OUTPUT_PATTERN));
  }
}
