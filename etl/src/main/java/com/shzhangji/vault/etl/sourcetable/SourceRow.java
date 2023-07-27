package com.shzhangji.vault.etl.sourcetable;

import com.shzhangji.vault.etl.extractconfig.ExtractConfig;
import com.shzhangji.vault.etl.targettable.TargetRow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.Value;

@Value
public class SourceRow {
  SourceTable sourceTable;
  Map<String, String> columns;
  RowKind rowKind;

  /**
   * One source row may generate multiple target rows.
   */
  public Stream<TargetRow> createTargetRows(List<ExtractConfig> extractConfigs) {
    return extractConfigs.stream().flatMap(config -> {
      if (rowKind == RowKind.DELETE && config.isIgnoreDelete()) {
        return Stream.empty();
      }

      var partitionColumns = config.getPartitionExtractor().extract(sourceTable, columns);

      var primaryKeys = new ArrayList<>(config.getPrimaryKeys());
      primaryKeys.addAll(partitionColumns.keySet());

      var targetColumns = new HashMap<>(columns);
      targetColumns.putAll(partitionColumns);

      if (rowKind == RowKind.DELETE) {
        targetColumns.keySet().removeIf(key -> !primaryKeys.contains(key));
      } else {
        targetColumns.keySet().removeIf(key -> !primaryKeys.contains(key) && !config.getExtractColumns().contains(key));
      }

      return Stream.of(new TargetRow(
        config.getTargetInstanceId(),
        config.getTargetDatabase(),
        config.getTargetTable(),
        primaryKeys,
        targetColumns,
        rowKind));
    });
  }
}
