package com.shzhangji.vault.canal.kafkasource;

import com.alibaba.otter.canal.client.CanalMessageDeserializer;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.shzhangji.vault.etl.sourcetable.SourceTable;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;

@RequiredArgsConstructor
public class SourceMessageExtractor extends RichFlatMapFunction<KafkaRecord, SourceMessage> {
  private final Map<String, Integer> topicInstances;

  @Override
  public void flatMap(KafkaRecord value, Collector<SourceMessage> out) throws Exception {
    var instanceId = topicInstances.get(value.getTopic());
    if (instanceId == null) {
      return;
    }

    var message = CanalMessageDeserializer.deserializer(value.getValue());
    for (var entry : message.getEntries()) {
      if (entry.getEntryType() != CanalEntry.EntryType.ROWDATA) {
        continue;
      }

      var header = entry.getHeader();
      var sourceTable = new SourceTable(
          instanceId,
          header.getSchemaName(),
          header.getTableName());

      out.collect(new SourceMessage(sourceTable, entry.getStoreValue().toByteArray()));
    }
  }
}
