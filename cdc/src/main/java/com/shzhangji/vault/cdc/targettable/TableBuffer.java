package com.shzhangji.vault.cdc.targettable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is not thread-safe.
 */
@Slf4j
@RequiredArgsConstructor
public class TableBuffer {
  private final TargetTable table;
  private final DataSource dataSource;
  private final TargetRowSinkOptions options;
  private final Map<List<String>, TargetRow> reducedBuffer = new HashMap<>();

  @Getter
  private long lastUsed = System.currentTimeMillis();

  public void addRow(TargetRow row) {
    var primaryKeyValues = row.getPrimaryKeys().stream()
      .map(key -> row.getColumns().get(key))
      .collect(Collectors.toList());

    reducedBuffer.put(primaryKeyValues, row);
    if (reducedBuffer.size() >= options.getMaxRows()) {
      flush();
    }

    lastUsed = System.currentTimeMillis();
  }

  public void flush() {
    if (reducedBuffer.isEmpty()) {
      return;
    }

    var replaceRows = new ArrayList<TargetRow>();
    var deleteRows = new ArrayList<TargetRow>();
    for (var row : reducedBuffer.values()) {
      switch (row.getRowKind()) {
        case REPLACE:
          replaceRows.add(row);
          break;
        case DELETE:
          deleteRows.add(row);
          break;
      }
    }

    executeReplaces(replaceRows);
    executeDeletes(deleteRows);
    reducedBuffer.clear();
  }

  private void executeReplaces(List<TargetRow> rows) {
    if (rows.isEmpty()) {
      return;
    }

    var sql = generateReplaceSql(rows);
    for (int retries = 0; retries < options.getMaxRetries(); ++retries) {
      try (var conn = dataSource.getConnection();
           var stmt = conn.createStatement()) {

        stmt.setQueryTimeout(options.getQueryTimeoutSecs());
        stmt.executeUpdate(sql);
        break;

      } catch (SQLException e) {
        if (retries == options.getMaxRetries() - 1) {
          var row = rows.get(0);
          log.error("Fail to insert data, target table: {}.{}", row.getDatabase(), row.getTable());
          throw new RuntimeException(e);
        }
      }
    }
  }

  private void executeDeletes(List<TargetRow> rows) {
    if (rows.isEmpty()) {
      return;
    }

    // Assume primary key does not change.
    var primaryKeys = rows.get(0).getPrimaryKeys();

    var sql = generateDeleteSql(primaryKeys);
    for (int retries = 0; retries <= options.getMaxRetries(); ++retries) {
      try (var conn = dataSource.getConnection();
           var stmt = conn.prepareStatement(sql)) {

        for (var row : rows) {
          for (int i = 0; i < primaryKeys.size(); ++i) {
            var value = row.getColumns().get(primaryKeys.get(i));
            stmt.setString(i + 1, value);
          }
          stmt.addBatch();
        }

        stmt.setQueryTimeout(options.getQueryTimeoutSecs());
        stmt.executeBatch();

      } catch (SQLException e) {
        if (retries == options.getMaxRetries() - 1) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  protected String generateReplaceSql(List<TargetRow> rows) {
    // Make sure column order is fixed.
    var columnNames = List.copyOf(rows.get(0).getColumns().keySet());

    var columnNamesExpr = columnNames.stream()
      .map(key -> "`" + key + "`")
      .collect(Collectors.joining(", "));

    var valuesExpr = rows.stream()
      .map(row -> "(" + generateColumnValuesExpr(columnNames, row.getColumns()) + ")")
      .collect(Collectors.joining(","));

    return String.format("REPLACE INTO `%s`.`%s` (%s) VALUES %s",
      table.getDatabase(), table.getTable(), columnNamesExpr, valuesExpr);
  }

  protected String generateDeleteSql(List<String> primaryKeys) {
    var where = primaryKeys.stream()
      .map(key -> "`" + key + "` = ?")
      .collect(Collectors.joining(" AND "));
    return String.format("DELETE FROM `%s`.`%s` WHERE %s", table.getDatabase(), table.getTable(), where);
  }

  protected String generateColumnValuesExpr(List<String> columnNames, Map<String, String> columnMap) {
    return columnNames.stream()
      .map(columnMap::get)
      .map(value -> {
        if (value == null) {
          return "NULL";
        }
        return "'" + escape(value) +  "'";
      })
      .collect(Collectors.joining(", "));
  }

  protected String escape(String value) {
    return value.replace("\\", "\\\\")
      .replace("'", "\\'")
      .replace("\r", "\\r")
      .replace("\n", "\\n");
  }
}
