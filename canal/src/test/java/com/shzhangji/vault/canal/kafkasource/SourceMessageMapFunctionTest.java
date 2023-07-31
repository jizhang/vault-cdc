package com.shzhangji.vault.canal.kafkasource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.alibaba.otter.canal.protocol.CanalEntry;
import org.junit.jupiter.api.Test;

class SourceMessageMapFunctionTest {
  @Test
  public void testConvertValue() {
    var fn = new SourceMessageMapFunction(1);

    assertEquals("", fn.convertValue(buildColumn("VARCHAR(255)", "")));
    assertEquals("a", fn.convertValue(buildColumn("VARCHAR(255)", "a")));

    assertEquals("", fn.convertValue(buildColumn("TEXT", "")));
    assertEquals("", fn.convertValue(buildColumn("longblob", "")));

    assertEquals("1", fn.convertValue(buildColumn("INT", "1")));
    assertNull(fn.convertValue(buildColumn("INT", "")));

    assertEquals("0000-00-00 00:00:00", fn.convertValue(buildColumn("TIMESTAMP", "0000-00-00 00:00:00")));
    assertNull(fn.convertValue(buildColumn("TIMESTAMP", "")));
  }

  private CanalEntry.Column buildColumn(String mysqlType, String value) {
    return CanalEntry.Column.newBuilder()
      .setMysqlType(mysqlType)
      .setValue(value)
      .build();
  }
}
