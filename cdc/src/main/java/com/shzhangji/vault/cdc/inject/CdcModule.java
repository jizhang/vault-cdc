package com.shzhangji.vault.cdc.inject;

import com.google.inject.AbstractModule;

public class CdcModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new ConfigModule());
    install(new DbModule());
  }
}
