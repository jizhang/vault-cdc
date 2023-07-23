package com.shzhangji.vault.cdc.db;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseInstanceRepository
    extends PagingAndSortingRepository<DatabaseInstance, Integer> {
  boolean existsByName(String name);
}
