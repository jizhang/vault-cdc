package com.shzhangji.vault.cdc.extractconfig;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.shzhangji.vault.cdc.sourcetable.SourceTable;
import java.io.Closeable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;

@Slf4j
@Singleton
public class ExtractConfigCache implements Closeable {
  private final SqlSessionFactory sessionFactory;

  private ScheduledExecutorService executor;
  @Setter(AccessLevel.PROTECTED)
  private volatile List<ExtractConfig> extractConfigs;
  private List<ExtractConfigRow> previousRows;

  private final LoadingCache<SourceTable, List<ExtractConfig>> cache = CacheBuilder.newBuilder()
      .expireAfterWrite(Duration.ofDays(1))
      .build(new CacheLoader<>() {
        @Override
        public List<ExtractConfig> load(@Nonnull SourceTable key) throws Exception {
          return getList0(key);
        }
      });

  @Inject
  public ExtractConfigCache(@Named("dwStageSessionFactory") SqlSessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  /**
   * Load extract configs from database. Startup a background thread to reload configs periodically.
   */
  public synchronized void initialize(int tenantId) {
    if (executor != null) {
      return;
    }

    log.info("Initialize with tenant ID {}", tenantId);

    reload(tenantId);

    executor = Executors.newSingleThreadScheduledExecutor();
    executor.scheduleWithFixedDelay(() -> {
      try {
        reload(tenantId);
      } catch (Exception e) {
        log.error("Fail to reload extract configs.", e);
      }
    }, 1, 1, TimeUnit.MINUTES);
  }

  public synchronized void close() {
    if (executor == null) {
      return;
    }

    executor.shutdown();
    executor = null;

    cache.cleanUp();
  }

  private synchronized void reload(int tenantId) {
    List<ExtractConfigRow> rows;
    try (var session = sessionFactory.openSession()) {
      var mapper = session.getMapper(ExtractConfigMapper.class);
      rows = mapper.getList(tenantId);
    }

    if (previousRows != null && previousRows.equals(rows)) {
      return;
    }

    var configs = rows.stream()
        .map(ExtractConfig::from)
        .collect(Collectors.toList());
    log.info("Loading {} extract configs.", configs.size());

    configs.stream()
        .filter(config -> config.getExtractColumns().isEmpty())
        .collect(Collectors.groupingBy(ExtractConfig::getTargetInstanceId))
        .forEach(this::fixExtractColumns);

    extractConfigs = configs;
    cache.invalidateAll();
    previousRows = rows;
  }

  public List<ExtractConfig> getList(SourceTable sourceTable) {
    return cache.getUnchecked(sourceTable);
  }

  protected List<ExtractConfig> getList0(SourceTable sourceTable) {
    Preconditions.checkNotNull(extractConfigs, "Extract configs is not initialized.");
    return extractConfigs.stream()
        .filter(config -> config.getSourceInstanceId() == sourceTable.getInstanceId())
        .filter(config -> config.getSourceDatabase().equals(sourceTable.getDatabase()))
        .filter(config -> {
          if (config.getSourceTableRegex() != null) {
            return config.getSourceTableRegex().matcher(sourceTable.getTable()).matches();
          } else {
            return config.getSourceTable().equals(sourceTable.getTable());
          }
        })
        .collect(Collectors.toList());
  }

  private void fixExtractColumns(int instanceId, List<ExtractConfig> configs) {
    DbInstanceRow instance;
    try (var session = sessionFactory.openSession()) {
      var mapper = session.getMapper(DbInstanceMapper.class);
      instance = mapper.get(instanceId)
          .orElseThrow(() -> new IllegalArgumentException("Target instance [" + instanceId + "] not found."));
    }

    try (var conn = getConnectionForInstance(instance)) {
      for (var config : configs) {
        var columns = getColumnsFromInstance(conn, config.getTargetDatabase(), config.getTargetTable());
        config.setExtractColumns(columns);
        log.info("Fix extract columns for {}.{} {}", config.getTargetDatabase(), config.getTargetTable(), columns);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Fail to get columns from target instance [" + instanceId + "].", e);
    }
  }

  private List<String> getColumnsFromInstance(Connection conn, String database, String table) throws SQLException {
    var sql = String.format("DESC `%s`.`%s`", database, table);
    var columns = new ArrayList<String>();
    try (var stmt = conn.createStatement(); var rs = stmt.executeQuery(sql)) {
      while (rs.next()) {
        columns.add(rs.getString(1).toLowerCase());
      }
    }
    return columns;
  }

  private Connection getConnectionForInstance(DbInstanceRow instance) throws SQLException {
    var url = String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=UTF-8",
        instance.getHost(), instance.getPort(), instance.getDatabase());
    return DriverManager.getConnection(url, instance.getUsername(), instance.getPassword());
  }
}
