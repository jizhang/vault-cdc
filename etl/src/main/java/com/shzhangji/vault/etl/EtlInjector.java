package com.shzhangji.vault.etl;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class EtlInjector {
  private static class Holder {
    static final Injector INJECTOR = Guice.createInjector(new EtlModule());
  }

  public static Injector getInjector() {
    return Holder.INJECTOR;
  }
}
