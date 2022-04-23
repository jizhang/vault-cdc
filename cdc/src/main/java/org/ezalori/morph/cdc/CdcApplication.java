package org.ezalori.morph.cdc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CdcApplication {
  private static class Holder {
    static final ApplicationContext INSTANCE = SpringApplication.run(CdcApplication.class);
  }

  public static ApplicationContext getInstance() {
    return Holder.INSTANCE;
  }

  @Bean
  String getString() {
    return "hello world";
  }
}
