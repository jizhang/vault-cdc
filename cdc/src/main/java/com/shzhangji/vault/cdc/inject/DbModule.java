package com.shzhangji.vault.cdc.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.io.IOException;
import java.util.Properties;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class DbModule extends AbstractModule {
  private static final String MYBATIS_CONFIG = "mybatis-config.xml";

  @Provides
  @Singleton
  @Named("vaultSessionFactory")
  public SqlSessionFactory provideVaultSessionFactory(Properties props) throws IOException {
    return new SqlSessionFactoryBuilder()
        .build(Resources.getResourceAsStream(MYBATIS_CONFIG), "vault", props);
  }
}
