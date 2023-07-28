package com.shzhangji.vault.cdc.sourcetable;

import com.shzhangji.vault.etl.EtlInjector;
import com.shzhangji.vault.etl.extractconfig.ExtractConfigCache;
import com.shzhangji.vault.etl.sourcetable.SourceRow;
import com.shzhangji.vault.etl.targettable.TargetRow;
import lombok.RequiredArgsConstructor;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

@RequiredArgsConstructor
public class SourceRowMapFunction extends RichFlatMapFunction<SourceRow, TargetRow> {
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
  public void flatMap(SourceRow sourceRow, Collector<TargetRow> out) throws Exception {
    var extractConfigs = extractConfigCache.getList(sourceRow.getSourceTable());
    if (!extractConfigs.isEmpty()) {
      sourceRow.createTargetRows(extractConfigs).forEach(out::collect);
    }
  }
}
