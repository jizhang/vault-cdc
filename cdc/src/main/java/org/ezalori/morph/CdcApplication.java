package org.ezalori.morph;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class CdcApplication {
  private static class Holder {
    static final ApplicationContext INSTANCE = SpringApplication.run(CdcApplication.class);
  }

  public static ApplicationContext getInstance() {
    return Holder.INSTANCE;
  }
}
