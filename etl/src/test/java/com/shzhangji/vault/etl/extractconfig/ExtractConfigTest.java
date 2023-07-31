package com.shzhangji.vault.etl.extractconfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class ExtractConfigTest {
  @Test
  public void testSplitColumns() {
    assertEquals(List.of(), ExtractConfig.splitColumns(""));
    assertEquals(List.of("a"), ExtractConfig.splitColumns("a"));
    assertEquals(List.of("a", "b"), ExtractConfig.splitColumns("a,,B"));
  }
}
