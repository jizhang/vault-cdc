package org.ezalori.morph.common.repository;

import org.ezalori.morph.common.model.DatabaseInstance;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseInstanceRepository
    extends PagingAndSortingRepository<DatabaseInstance, Integer> {
  boolean existsByName(String name);
}
