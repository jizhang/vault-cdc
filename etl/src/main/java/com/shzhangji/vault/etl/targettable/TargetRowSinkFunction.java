package com.shzhangji.vault.etl.targettable;

import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

@Slf4j
@RequiredArgsConstructor
public class TargetRowSinkFunction extends RichSinkFunction<TargetRow>
    implements CheckpointedFunction {

  private static final long TABLE_BUFFER_EXPIRE_MILLIS = TimeUnit.DAYS.toMillis(1);

  private final TargetRowSinkOptions options;

  private transient HikariDataSource dataSource;
  private transient Map<TargetTable, TableBuffer> tableBuffers;
  private transient ScheduledExecutorService executor;
  private transient volatile Exception flushException;

  @Override
  public synchronized void open(Configuration parameters) throws Exception {
    createDataSource(getRuntimeContext().getIndexOfThisSubtask());
    createTableBuffers();
  }

  @Override
  public synchronized void invoke(TargetRow value, Context context) throws Exception {
    checkFlushException();

    var targetTable = new TargetTable(value.getDatabase(), value.getTable());
    var tableBuffer = tableBuffers.computeIfAbsent(
        targetTable, k -> new TableBuffer(k, dataSource, options));
    tableBuffer.addRow(value);
  }

  @Override
  public void close() throws Exception {
    if (executor != null) {
      executor.shutdown();
    }

    try {
      flush();
    } finally {
      dataSource.close();
    }
  }

  @Override
  public void initializeState(FunctionInitializationContext context) throws Exception {

  }

  @Override
  public void snapshotState(FunctionSnapshotContext context) throws Exception {
    flush();
  }

  private void createDataSource(int subtaskIndex) {
    dataSource = new HikariDataSource();
    dataSource.setPoolName(String.format("TargetRowSink-%d-%d",
        options.getInstanceId(), subtaskIndex));
    dataSource.setJdbcUrl(options.getUrl());
    dataSource.setUsername(options.getUsername());
    dataSource.setPassword(options.getPassword());
    dataSource.setMaximumPoolSize(1);
    dataSource.addDataSourceProperty("queryTimeoutKillsConnection", "true");
  }

  private void createTableBuffers() {
    tableBuffers = new HashMap<>();
    flushException = null;

    if (options.getIntervalMillis() > 0) {
      executor = Executors.newSingleThreadScheduledExecutor();
      executor.scheduleWithFixedDelay(() -> {
        if (flushException != null) {
          return;
        }
        try {
          flush();
        } catch (Exception e) {
          flushException = e;
        }
      }, options.getIntervalMillis(), options.getIntervalMillis(), TimeUnit.MILLISECONDS);
    } else {
      executor = null;
    }
  }

  private void checkFlushException() {
    if (flushException != null) {
      throw new RuntimeException("Flush exception in background thread.", flushException);
    }
  }

  private synchronized void flush() {
    checkFlushException();
    tableBuffers.values().forEach(TableBuffer::flush);
    tableBuffers.entrySet().removeIf(entry -> {
      long sinceLastUsed = System.currentTimeMillis() - entry.getValue().getLastUsed();
      if (sinceLastUsed >= TABLE_BUFFER_EXPIRE_MILLIS) {
        log.info("Table buffer of {} is expired.", entry.getKey());
        return true;
      }
      return false;
    });
  }
}
