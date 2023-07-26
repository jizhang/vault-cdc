package com.shzhangji.vault.cdc.extractconfig;

import java.util.Optional;

public interface DbInstanceMapper {
  Optional<DbInstanceRow> get(int id);
}
