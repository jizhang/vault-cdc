package org.ezalori.morph.web.repository;

import org.ezalori.morph.web.model.ExtractTable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtractTableRepository extends CrudRepository<ExtractTable, Integer> {
}
