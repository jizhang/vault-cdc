package com.shzhangji.vault.canal.kafkasource;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.shzhangji.vault.etl.EtlInjector;
import com.shzhangji.vault.etl.extractconfig.ExtractConfig;
import com.shzhangji.vault.etl.extractconfig.ExtractConfigCache;
import com.shzhangji.vault.etl.sourcetable.RowKind;
import com.shzhangji.vault.etl.sourcetable.SourceRow;
import com.shzhangji.vault.etl.sourcetable.SourceTable;
import com.shzhangji.vault.etl.targettable.TargetRow;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

@RequiredArgsConstructor
public class SourceMessageMapFunction extends RichFlatMapFunction<SourceMessage, TargetRow> {
  private static final Pattern ALLOW_EMPTY =
      Pattern.compile("(CHAR|TEXT|BLOB|BINARY)", Pattern.CASE_INSENSITIVE);

  private final int tenantId;

  private transient ExtractConfigCache extractConfigCache;

  @Override
  public void open(Configuration parameters) throws Exception {
    var injector = EtlInjector.getInjector();
    extractConfigCache = injector.getInstance(ExtractConfigCache.class);
    extractConfigCache.initialize(tenantId);
  }

  @Override
  public void close() throws Exception {
    extractConfigCache.close();
  }

  @Override
  public void flatMap(SourceMessage value, Collector<TargetRow> out) throws Exception {
    var extractConfigs = extractConfigCache.getList(value.getSourceTable());
    if (extractConfigs.isEmpty()) {
      return;
    }

    var rowChange = CanalEntry.RowChange.parseFrom(value.getRowChangeData());
    createRowsFromChange(rowChange, value.getSourceTable(), extractConfigs).forEach(out::collect);
  }

  /**
   * One entry may contain multiple source rows.
   */
  private Stream<TargetRow> createRowsFromChange(
      CanalEntry.RowChange rowChange, SourceTable sourceTable, List<ExtractConfig> extractConfigs) {

    var rowKindOpt = getRowKind(rowChange.getEventType());
    if (rowKindOpt.isEmpty()) {
      return Stream.empty();
    }
    var rowKind = rowKindOpt.get();

    return rowChange.getRowDatasList().stream()
        .map(rowData -> {
          var columnList = rowKind == RowKind.DELETE ? rowData.getBeforeColumnsList() :
              rowData.getAfterColumnsList();
          var columnMap = new HashMap<String, String>();
          for (var column : columnList) {
            columnMap.put(column.getName().toLowerCase(), convertValue(column));
          }

          return new SourceRow(sourceTable, columnMap, rowKind);
        })
        .flatMap(sourceRow -> sourceRow.createTargetRows(extractConfigs));
  }

  private Optional<RowKind> getRowKind(CanalEntry.EventType eventType) {
    switch (eventType) {
      case INSERT:
      case UPDATE:
        return Optional.of(RowKind.REPLACE);
      case DELETE:
        return Optional.of(RowKind.DELETE);
      default:
        return Optional.empty();
    }
  }

  /**
   * NULL values are represented as empty string. Convert them back to null.
   */
  protected String convertValue(CanalEntry.Column column) {
    var value = column.getValue();
    if (value.length() > 0) {
      return value;
    }

    var mo = ALLOW_EMPTY.matcher(column.getMysqlType());
    return mo.find() ? value : null;
  }
}
