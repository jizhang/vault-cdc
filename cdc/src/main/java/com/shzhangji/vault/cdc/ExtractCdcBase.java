package com.shzhangji.vault.cdc;

import com.google.common.base.Preconditions;
import com.shzhangji.vault.cdc.sourcetable.SourceRowDeserializer;
import com.shzhangji.vault.cdc.sourcetable.SourceRowMapFunction;
import com.shzhangji.vault.etl.EtlInjector;
import com.shzhangji.vault.etl.extractconfig.DbInstanceService;
import com.shzhangji.vault.etl.sourcetable.SourceRow;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

@RequiredArgsConstructor
public class ExtractCdcBase {
  private final ExtractCdcOptions options;

  private DbInstanceService instanceService;
  private StreamExecutionEnvironment env;

  public void run() throws Exception {
    var injector = EtlInjector.getInjector();
    var props = injector.getInstance(Properties.class);
    instanceService = injector.getInstance(DbInstanceService.class);

    env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setStateBackend(new HashMapStateBackend());
    env.enableCheckpointing(30_000);
    env.getCheckpointConfig().setCheckpointStorage(
        props.getProperty("flink.checkpoints.dir") + "/" + options.getJobName());

    // Fetch source rows from possibly multiple sources.
    var sourceRows = createSourceRows();

    // Match target instance.
    var targetRows = sourceRows.flatMap(new SourceRowMapFunction(options.getTenantId()));

    // Create sink for each target instance, which can be database or message queue.
    instanceService.sinkToVault(targetRows);

    env.execute(options.getJobName());
  }

  private DataStream<SourceRow> createSourceRows() {
    Preconditions.checkArgument(options.getSourceInstanceList().size() > 0, "Source instance list cannot be empty.");

    var sourceInstanceCount = options.getSourceInstanceList().stream()
      .map(SourceInstance::getInstanceId)
      .distinct()
      .count();
    Preconditions.checkArgument(sourceInstanceCount == options.getSourceInstanceList().size(), "Duplicate source instances are not allowed.");

    DataStream<SourceRow> dataStream = null;
    for (var sourceInstance : options.getSourceInstanceList()) {
      var sourceInstanceRow = instanceService.getInstance(sourceInstance.getInstanceId());
      var source = MySqlSource.<SourceRow>builder()
        .hostname(sourceInstanceRow.getHost())
        .port(sourceInstanceRow.getPort())
        .username(sourceInstanceRow.getUsername())
        .password(sourceInstanceRow.getPassword())
        .databaseList(sourceInstance.getDatabaseList().toArray(String[]::new))
        .tableList(sourceInstance.getTableList().toArray(String[]::new))
        .deserializer(new SourceRowDeserializer(sourceInstance.getInstanceId()))
        .startupOptions(StartupOptions.latest())
        .build();

      var sourceRows = env.fromSource(source, WatermarkStrategy.noWatermarks(), "mysql-" + sourceInstance.getInstanceId())
        .setParallelism(1);

      if (dataStream == null) {
        dataStream = sourceRows;
      } else {
        dataStream = dataStream.union(sourceRows);
      }
    }

    assert dataStream != null;
    return dataStream.keyBy(SourceRow::getSourceTable);
  }
}
