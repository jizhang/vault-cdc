package com.shzhangji.vault.etl;

import com.google.inject.AbstractModule;
import com.shzhangji.vault.common.ConfigModule;

public class EtlModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new ConfigModule());
    install(new DbModule());
  }
}
