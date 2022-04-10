package org.ezalori.morph.web.repository;

import org.ezalori.morph.web.model.DatabaseInstance;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseInstanceRepository extends CrudRepository<DatabaseInstance, Integer> {
}
