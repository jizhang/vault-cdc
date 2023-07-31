package com.shzhangji.vault.etl.targettable;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.shzhangji.vault.etl.sourcetable.RowKind;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TableBufferTest {
  @Mock
  private DataSource dataSource;
  private TableBuffer tableBuffer;

  @BeforeEach
  public void setup() {
    var table = new TargetTable("dw", "zj_account_logs");
    var options = TargetRowSinkOptions.builder().build();
    tableBuffer = new TableBuffer(table, dataSource, options);
  }

  @Test
  public void testEscape() {
    assertEquals("\\'", tableBuffer.escape("'"));
  }

  @Test
  public void testGenerateColumnValuesExpr() {
    var expr = tableBuffer.generateColumnValuesExpr(List.of("id", "user_id", "dt"),
        Map.of("id", "1", "user_id", "1001", "dt", "202110"));
    assertEquals("'1', '1001', '202110'", expr);
  }

  @Test
  public void testGenerateDeleteSql() {
    assertEquals("DELETE FROM `dw`.`zj_account_logs` WHERE `id` = ? AND `dt` = ?",
        tableBuffer.generateDeleteSql(List.of("id", "dt")));
  }

  @Test
  public void testGenerateReplaceSql() {
    var rows = List.of(
      new TargetRow(
        3,
        "dw",
        "zj_account_logs",
        List.of("id", "dt"),
        ImmutableMap.of("id", "1", "user_id", "1001", "dt", "202110"),
        RowKind.REPLACE),
      new TargetRow(
        3,
        "dw",
        "zj_account_logs",
        List.of("id", "dt"),
        ImmutableMap.of("id", "2", "user_id", "1002", "dt", "202110"),
        RowKind.REPLACE));

    var expectedSql = "REPLACE INTO `dw`.`zj_account_logs` " +
      "(`id`, `user_id`, `dt`) " +
      "VALUES " +
      "('1', '1001', '202110')," +
      "('2', '1002', '202110')";
    assertEquals(expectedSql, tableBuffer.generateReplaceSql(rows));
  }
}
