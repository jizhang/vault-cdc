package com.shzhangji.vault.etl.extractconfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.shzhangji.vault.etl.sourcetable.SourceTable;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExtractConfigCacheTest {
  @Mock
  private SqlSessionFactory sessionFactory;
  private ExtractConfigCache cache;

  @BeforeEach
  public void setup() {
    cache = new ExtractConfigCache(sessionFactory);
  }

  @Test
  public void testGetList0() {
    cache.setExtractConfigs(List.of());
    assertEquals(List.of(), cache.getList0(new SourceTable(1, "zhuanqian", "account_logs_202110")));

    var config = new ExtractConfig();
    config.setSourceInstanceId(1);
    config.setSourceDatabase("zhuanqian");
    config.setSourceTableRegex(Pattern.compile("account_logs_(\\d+)"));
    cache.setExtractConfigs(List.of(config));
    assertEquals(List.of(config), cache.getList0(new SourceTable(1, "zhuanqian", "account_logs_202110")));
  }
}
