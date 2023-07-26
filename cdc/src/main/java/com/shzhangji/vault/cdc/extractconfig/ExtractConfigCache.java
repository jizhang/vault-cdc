package com.shzhangji.vault.cdc.extractconfig;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.io.Closeable;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;

@Slf4j
@Singleton
public class ExtractConfigCache implements Closeable {
  private SqlSessionFactory sessionFactory;

  @Inject
  public ExtractConfigCache(@Named("vaultSessionFactory") SqlSessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void close() throws IOException {
    try (var session = sessionFactory.openSession()) {
      var mapper = session.getMapper(ExtractConfigMapper.class);
      var list = mapper.getList(TenantId.VAULT);
      if (list.isEmpty()) {
        throw new IllegalStateException("Extract config list is empty.");
      }
    }
  }
}
