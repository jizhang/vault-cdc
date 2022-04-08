package org.ezalori.morph.client.domain.parser;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.alibaba.otter.canal.protocol.CanalEntry.Header;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.ezalori.morph.client.domain.mysql.MysqlMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by hey on 01/02/2018.
 */
public class MysqlParser implements Parser {

  private static Logger logger = LoggerFactory.getLogger(MysqlParser.class);
  protected static final String BLANK = " ";

  protected static final String COMMA = ",";

  protected static final String NONE_STR = "";

  protected static final String EQUALS = "=";

  protected static final String SINGLE_QUOTATION = "\'";

  protected MysqlMapping dbMapping;

  public MysqlParser() {
  }

  public MysqlParser(MysqlMapping dbMapping) {
    this.dbMapping = dbMapping;
  }

  @Override
  public List<String> getSqlFromEntry(Header header, RowChange rowChange) throws SQLException {
    List<String> upsertSql = null;
    String schema = "";
    String table = "";

    // real-time
    if (dbMapping != null) {
      schema = dbMapping.getTargetSchema();
      table = dbMapping.getTargetTable();
      Set<String> fields = dbMapping.getFields();

      upsertSql = getSqlStrByRowData(rowChange, schema, table, fields);
    }

    return upsertSql;

  }


  protected List<String> getSqlStrByRowData(RowChange rowChange, String schema,
                                            String table, Set<String> fieldsSet) {
    List<String> upsertStrList = new ArrayList<>();
    EventType eventType = rowChange.getEventType();

    for (RowData rowData : rowChange.getRowDatasList()) {

      if (eventType == EventType.DELETE) {
        // upsertStrList.add(generateDeleteStr(rowData, schema, table));
        continue;
      } else {
        upsertStrList.add(generateUpsertStr(rowData, schema, table, fieldsSet));
      }

    }
    return upsertStrList;
  }


  protected String generateUpsertStr(RowData rowData, String schema, String table,
                                     Set<String> fieldsSet) {
    StringBuilder columnNamesBuilder = new StringBuilder();
    StringBuilder valuesBuilder = new StringBuilder();
    StringBuilder updateValueBuilder = new StringBuilder(NONE_STR);
    String ctColumnName = "";
    String ctValue = "";
    String ctType = "";

    for (CanalEntry.Column column : rowData.getAfterColumnsList()) {

      if (fieldsSet.contains(column.getName())) {
        //value check when type unknow
        String type = column.getUnknownFields().toString();
        String value = column.getValue();

        partialJoin(columnNamesBuilder, valuesBuilder, updateValueBuilder,
            column.getName(), value, type);

      }
    }

    return sqlJoin(columnNamesBuilder, valuesBuilder, updateValueBuilder, schema, table);
  }

  protected void partialJoin(StringBuilder columnNamesBuilder, StringBuilder valuesBuilder,
                             StringBuilder updateValueBuilder, String name, String value,
                             String type) {
    columnNamesBuilder.append(name)
      .append(COMMA);
    value = valueCheck(type, value);
    valuesBuilder.append(value)
      .append(COMMA);
    updateValueBuilder.append(name)
      .append(EQUALS)
      .append(value)
      .append(COMMA);
  }

  protected String sqlJoin(StringBuilder columnNamesBuilder, StringBuilder valuesBuilder,
                           StringBuilder updateValueBuilder, String schema, String table) {
    String columnNames = columnNamesBuilder.toString();
    columnNames = columnNames.substring(0, columnNames.length() - 1);
    String values = valuesBuilder.toString();
    values = values.substring(0, values.length() - 1);
    String updateValue = updateValueBuilder.toString();
    updateValue = updateValueBuilder.substring(0, updateValue.length() - 1);

    String sqlStr = String.format(
        "INSERT INTO %s.%s (%s) VALUES (%s) ON DUPLICATE KEY UPDATE %s", schema,
        table, columnNames, values, updateValue);

    return sqlStr;
  }

  protected String valueCheck(String type, String value) {
    value = dataWash(value);
    if (type.contains("char")
        || type.contains("text")
        || type.contains("datetime")
        || type.contains("CHAR")
        || type.contains("TEXT")
        || type.contains("DATETIME")) {
      value = SINGLE_QUOTATION + value + SINGLE_QUOTATION;
    }

    if (type.contains("timestamp") || type.contains("TIMESTAMP")) {
      if (value != null) {
        if (value.equals(NONE_STR)) {
          value = null;
        } else {
          value = SINGLE_QUOTATION + value + SINGLE_QUOTATION;
        }
      }
    }

    return value;
  }

  protected String dataWash(String value) {

    if (value != null) {
      if (value.contains("\\")) {
        value = value.replace("\\", "\\\\");
      }
      if (value.contains("'")) {
        value = value.replace("'", "\\'");
      }
    }

    return value;
  }

  /**
   * 获取ddl的sql.
   *
   * @param rowChange the change of entries
   * @return ddl sql
   */
  private String getSqlStr(RowChange rowChange) {
    return rowChange.getSql();
  }

  private String generateDeleteStr(RowData rowData, String schema, String table) {
    String keyStr = NONE_STR;
    StringBuilder keyStrBuilder = new StringBuilder(keyStr);

    for (CanalEntry.Column column : rowData.getAfterColumnsList()) {

      if (column.getIsKey() && (!keyStr.equals(NONE_STR))) {
        keyStrBuilder.append(column.getName())
          .append(EQUALS)
          .append(column.getValue());
        keyStr = keyStrBuilder.toString();
        break;
      }
    }

    String sqlStr = String.format("DELETE %s.%s WHERE %s", schema, table, keyStr);
    return sqlStr;
  }

  private String generateInsertStr(RowData rowData, Header header) {
    String schema = header.getSchemaName();
    String table = header.getTableName();
    StringBuilder columnNamesBuilder = new StringBuilder();
    StringBuilder valuesBuilder = new StringBuilder();

    for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
      columnNamesBuilder.append(column.getName())
        .append(COMMA);
      valuesBuilder.append(column.getValue())
        .append(COMMA);
    }

    String columnNames = columnNamesBuilder.substring(0, columnNamesBuilder.length() - 1);
    String values = valuesBuilder.substring(0, valuesBuilder.length() - 1);
    String sqlStr = String.format("INSERT INTO %s.%s (%s) VALUES %s",
        schema, table, columnNames, values);
    return sqlStr;
  }

  private String generateUpdateStr(RowData rowData, Header header) {
    String schema = header.getSchemaName();
    String table = header.getTableName();
    String keyStr = NONE_STR;
    StringBuilder updateValuesBuilder = new StringBuilder(NONE_STR);
    StringBuilder keyStrBuilder = new StringBuilder(NONE_STR);

    for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
      updateValuesBuilder.append(column.getName())
        .append(EQUALS)
        .append(column.getValue())
        .append(COMMA);

      if (column.getIsKey() && !NONE_STR.equals(keyStr)) {
        keyStrBuilder.append(column.getName())
          .append(EQUALS)
          .append(column.getValue());
      }

    }
    String updateValues = updateValuesBuilder.toString();
    String sqlStr = String.format("UPDATE %s.%s SET %s WHERE %s",
        schema, table, updateValues, keyStr);
    return sqlStr;
  }

}
