package com.shzhangji.vault.etl.extractconfig;

import com.shzhangji.vault.etl.targettable.TargetRow;
import com.shzhangji.vault.etl.targettable.TargetRowSinkFunction;
import com.shzhangji.vault.etl.targettable.TargetRowSinkOptions;
import lombok.Data;
import org.apache.flink.streaming.api.datastream.DataStream;

@Data
public class DbInstanceRow {
  private int id;
  private String host;
  private int port;
  private String username;
  private String password;
  private String database;

  public void sink(DataStream<TargetRow> targetRows) {
    var targetJdbcUrl = String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=UTF-8",
        host, port, database);

    var sinkOptions = TargetRowSinkOptions.builder()
        .instanceId(id)
        .url(targetJdbcUrl)
        .username(username)
        .password(password)
        .maxRows(5000)
        .intervalMillis(3000)
        .maxRetries(3)
        .queryTimeoutSecs(60)
        .build();

    targetRows.filter(row -> row.getInstanceId() == sinkOptions.getInstanceId())
        .addSink(new TargetRowSinkFunction(sinkOptions)).name("sink-" + id);
  }
}
