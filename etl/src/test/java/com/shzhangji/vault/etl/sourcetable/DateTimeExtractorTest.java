package com.shzhangji.vault.etl.sourcetable;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;

class DateTimeExtractorTest {
  @Test
  public void testExtract() {
    var extractor = new DateTimeExtractor("added_at", "dt");
    var sourceTable = new SourceTable(1, "zhuanqian", "account_logs_202110");
    assertEquals(Map.of("dt", "20211001"), extractor.extract(sourceTable, Map.of("added_at", "2021-10-01 00:00:00")));
  }
}
