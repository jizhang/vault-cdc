package com.shzhangji.vault.etl.sourcetable;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class TableRegexExtractorTest {
  @Test
  public void testExtract() {
    var extractor = new TableRegexExtractor(Pattern.compile("account_logs_(\\d+)"), "dt");
    var sourceTable = new SourceTable(1, "zhuanqian", "account_logs_202110");
    assertEquals(Map.of("dt", "202110"), extractor.extract(sourceTable, Map.of()));
  }
}
