package com.shzhangji.vault.etl;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;

public class HikariDataSourceFactory extends UnpooledDataSourceFactory {
  public HikariDataSourceFactory() {
    this.dataSource = new HikariDataSource();
  }
}
