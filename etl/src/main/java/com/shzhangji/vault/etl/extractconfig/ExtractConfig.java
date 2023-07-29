package com.shzhangji.vault.etl.extractconfig;

import com.google.common.base.Preconditions;
import com.shzhangji.vault.etl.sourcetable.DateTimeExtractor;
import com.shzhangji.vault.etl.sourcetable.EmptyExtractor;
import com.shzhangji.vault.etl.sourcetable.PartitionExtractor;
import com.shzhangji.vault.etl.sourcetable.TableRegexExtractor;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class ExtractConfig {
  private int sourceInstanceId;
  private String sourceDatabase;
  private String sourceTable;
  private Pattern sourceTableRegex;
  private List<String> primaryKeys;
  private List<String> extractColumns;

  private int targetInstanceId;
  private String targetDatabase;
  private String targetTable;
  private PartitionExtractor partitionExtractor;

  private boolean ignoreDelete;

  public static ExtractConfig from(ExtractConfigRow row) {
    var config = new ExtractConfig();
    config.sourceInstanceId = row.getSourceInstanceId();
    config.sourceDatabase = row.getSourceDatabase();
    config.sourceTable = row.getSourceTable();

    if (row.isRegex()) {
      config.sourceTableRegex = Pattern.compile(row.getSourceTable());
    }

    config.primaryKeys = splitColumns(row.getPrimaryKeys());
    Preconditions.checkArgument(!config.primaryKeys.isEmpty(), "Primary keys cannot be empty.");

    config.extractColumns = splitColumns(row.getExtractColumns());
    if (!config.extractColumns.isEmpty()) {
      Preconditions.checkArgument(config.extractColumns.containsAll(config.getPrimaryKeys()),
          "Extract columns must include primary keys.");
    }

    config.targetInstanceId = row.getTargetInstanceId();
    config.targetDatabase = row.getTargetDatabase();
    config.targetTable = row.getTargetTable();

    switch (row.getPartitionType()) {
      case 0:
        config.partitionExtractor = new EmptyExtractor();
        break;

      case 1:
        Preconditions.checkArgument(StringUtils.isNoneEmpty(row.getPartitionInputColumn()));
        Preconditions.checkArgument(StringUtils.isNoneEmpty(row.getPartitionOutputColumn()));
        config.partitionExtractor = new DateTimeExtractor(
            row.getPartitionInputColumn(), row.getPartitionOutputColumn());
        break;

      case 2:
        Preconditions.checkNotNull(config.getSourceTableRegex());
        Preconditions.checkArgument(StringUtils.isNoneEmpty(row.getPartitionOutputColumn()));
        config.partitionExtractor = new TableRegexExtractor(
            config.getSourceTableRegex(), row.getPartitionOutputColumn());
        break;

      default:
        throw new IllegalArgumentException(
            "Partition type " + row.getPartitionType() + " is not implemented.");
    }

    config.ignoreDelete = row.isIgnoreDelete();

    return config;
  }

  protected static List<String> splitColumns(String delimited) {
    if (StringUtils.isEmpty(delimited)) {
      return List.of();
    }
    return Arrays.stream(delimited.split(","))
        .map(String::strip)
        .filter(StringUtils::isNoneEmpty)
        .map(String::toLowerCase)
        .collect(Collectors.toList());
  }
}
