package com.shzhangji.vault.cdc.table;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtractTableRepository extends PagingAndSortingRepository<ExtractTable, Integer> {
  @Query("SELECT COUNT(*) FROM extract_table"
      + " WHERE source_instance = :dbId OR target_instance = :dbId")
  int countByDbId(@Param("dbId") int dbId);
}
