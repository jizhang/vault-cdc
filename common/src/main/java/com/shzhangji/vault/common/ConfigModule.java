package com.shzhangji.vault.common;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigModule extends AbstractModule {
  private static final String[] CONFIG_RESOURCES = new String[] {
      "/config.properties",
      "/override/config.properties",
  };
  private static final String[] CONFIG_FILES = new String[] {
      "./conf/config.properties",
      "/home/www/vault-cdc/conf/config.properties",
  };

  @Provides
  @Singleton
  public Properties provideProperties() throws IOException {
    Properties props = new Properties();

    for (String configResource : CONFIG_RESOURCES) {
      try (InputStream in = getClass().getResourceAsStream(configResource)) {
        if (in != null) {
          props.load(in);
          log.info("Load from classpath {}", configResource);
        }
      }
    }

    for (String configFile : CONFIG_FILES) {
      try (InputStream in = new FileInputStream(configFile)) {
        props.load(in);
        log.info("Load from file {}", configFile);
      } catch (FileNotFoundException e) {
        // no-op
      }
    }

    return props;
  }
}
