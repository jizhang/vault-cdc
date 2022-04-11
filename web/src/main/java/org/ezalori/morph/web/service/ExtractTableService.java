package org.ezalori.morph.web.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.ezalori.morph.web.repository.DatabaseInstanceRepository;
import org.ezalori.morph.web.repository.ExtractTableRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExtractTableService {
  private final DatabaseInstanceRepository instanceRepo;
  private final ExtractTableRepository tableRepo;

  public List<String> getColumns(
      Integer sourceInstance, String sourceDatabase, String sourceTable) {

    var instanceOpt = instanceRepo.findById(sourceInstance);
    if (instanceOpt.isEmpty()) {
      return List.of();
    }

    var instance = instanceOpt.get();
    var url = String.format("jdbc:mysql://%s:%d/?useUnicode=true&characterEncoding=UTF-8",
        instance.getHost(), instance.getPort());

    var ds = new DriverManagerDataSource(url, instance.getUsername(), instance.getPassword());
    var jt = new JdbcTemplate(ds);

    return jt.queryForList(
        "SELECT column_name FROM information_schema.columns"
        + " WHERE table_schema = ? AND table_name = ?",
        String.class, sourceDatabase, sourceTable);
  }
}
