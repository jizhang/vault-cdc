package com.shzhangji.vault.etl.extractconfig;

import java.util.Optional;

public interface DbInstanceMapper {
  Optional<DbInstanceRow> get(int id);
}
