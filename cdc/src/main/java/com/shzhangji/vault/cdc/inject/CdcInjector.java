package com.shzhangji.vault.cdc.inject;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class CdcInjector {
  private static class Holder {
    static final Injector INJECTOR = Guice.createInjector(new CdcModule());
  }

  public static Injector getInjector() {
    return Holder.INJECTOR;
  }
}
