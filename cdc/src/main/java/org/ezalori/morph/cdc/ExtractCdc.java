package org.ezalori.morph.cdc;

import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.ezalori.morph.cdc.table.ExtractTableRepository;

@Slf4j
public class ExtractCdc {
  public static void main(String[] args) throws Exception {
    var context = Application.getInstance();
    var tableRepo = context.getBean(ExtractTableRepository.class);
    log.info("count: {}", tableRepo.count());

    var mysqlSource = MySqlSource.<String>builder()
        .hostname("localhost")
        .port(3306)
        .databaseList("morph")
        .tableList("morph\\..+")
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
