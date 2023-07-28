package com.shzhangji.vault.etl.extractconfig;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.shzhangji.vault.etl.targettable.TargetRow;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.ibatis.session.SqlSessionFactory;

@Singleton
public class DbInstanceService {
  private final SqlSessionFactory sessionFactory;

  @Inject
  public DbInstanceService(@Named("vaultSessionFactory") SqlSessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  public DbInstanceRow getInstance(int instanceId) {
    try (var session = sessionFactory.openSession()) {
      var mapper = session.getMapper(DbInstanceMapper.class);
      return mapper.get(instanceId).orElseThrow(
          () -> new IllegalArgumentException("InstanceId ID " + instanceId + " not found."));
    }
  }

  public void sinkToVault(DataStream<TargetRow> targetRows) {
    getInstance(DbInstanceId.VAULT).sink(targetRows);
  }
}
