package com.shzhangji.vault.canal;

import com.shzhangji.vault.canal.kafkasource.KafkaRecord;
import com.shzhangji.vault.canal.kafkasource.KafkaRecordDeserializer;
import com.shzhangji.vault.canal.kafkasource.SourceMessageExtractor;
import com.shzhangji.vault.canal.kafkasource.SourceMessageMapFunction;
import com.shzhangji.vault.etl.EtlInjector;
import com.shzhangji.vault.etl.extractconfig.DbInstanceService;
import java.util.List;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

@RequiredArgsConstructor
public class ExtractCanalBase {
  private final ExtractCanalOptions options;

  public void run() throws Exception {
    var injector = EtlInjector.getInjector();
    var instanceService = injector.getInstance(DbInstanceService.class);

    var env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setStateBackend(new HashMapStateBackend());
    env.enableCheckpointing(30_000);

    var props = injector.getInstance(Properties.class);
    env.getCheckpointConfig().setCheckpointStorage(
        props.getProperty("flink.checkpoints.dir") + "/" + options.getJobName());

    var source = KafkaSource.<KafkaRecord>builder()
      .setBootstrapServers(props.getProperty("kafka.user_action.servers"))
      .setTopics(List.copyOf(options.getTopicInstances().keySet()))
      .setGroupId(options.getGroupId())
      .setStartingOffsets(OffsetsInitializer.committedOffsets())
      .setDeserializer(new KafkaRecordDeserializer())
      .build();

    // If there are multiple Kafka sources, use DataStream#union().
    var kafkaRecords = env.fromSource(source, WatermarkStrategy.noWatermarks(), "canal");

    // Extract canal message.
    var sourceMessages = kafkaRecords.flatMap(new SourceMessageExtractor(options.getTopicInstances()));

    // Match target instance.
    var targetRows = sourceMessages.flatMap(new SourceMessageMapFunction(options.getTenantId()));

    // Target instance can be database or message queue.
    instanceService.sinkToVault(targetRows);

    env.execute(options.getJobName());
  }
}
