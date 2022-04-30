package org.ezalori.morph.cdc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@SpringBootApplication(scanBasePackages = "org.ezalori.morph")
@EntityScan("org.ezalori.morph")
@EnableJdbcRepositories("org.ezalori.morph")
public class CdcApplication {
  private static class Holder {
    static final ApplicationContext INSTANCE = SpringApplication.run(CdcApplication.class);
  }

  public static ApplicationContext getInstance() {
    return Holder.INSTANCE;
  }
}
