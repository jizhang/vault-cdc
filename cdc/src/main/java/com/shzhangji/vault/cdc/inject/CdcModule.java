package com.shzhangji.vault.cdc.inject;

import com.google.inject.AbstractModule;
import com.shzhangji.vault.common.ConfigModule;

public class CdcModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new ConfigModule());
    install(new DbModule());
  }
}
