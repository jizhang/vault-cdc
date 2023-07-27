package com.shzhangji.vault.cdc;

import com.shzhangji.vault.etl.EtlInjector;
import com.shzhangji.vault.etl.extractconfig.ExtractConfigCache;
import com.shzhangji.vault.etl.extractconfig.TenantId;
import com.shzhangji.vault.etl.sourcetable.SourceTable;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

@Slf4j
public class ExtractCdc {
  public static void main(String[] args) throws Exception {
    var injector = EtlInjector.getInjector();
    var cache = injector.getInstance(ExtractConfigCache.class);
    cache.initialize(TenantId.VAULT);
    var list = cache.getList(new SourceTable(1, "vault", "t_user"));
    if (list.isEmpty()) {
      cache.close();
      throw new IllegalStateException("Source table not found");
    }

    var mysqlSource = MySqlSource.<String>builder()
        .hostname("localhost")
        .port(3306)
        .databaseList("vault")
        .tableList("vault\\..+")
        .username("root")
        .password("")
        .deserializer(new JsonDebeziumDeserializationSchema())
        .startupOptions(StartupOptions.latest())
        .build();

    var env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.enableCheckpointing(3000);
    env.fromSource(mysqlSource, WatermarkStrategy.noWatermarks(), "source")
        .setParallelism(2)
        .print().setParallelism(1);

    env.execute();
  }
}
