package com.shzhangji.vault.etl.extractconfig;

import java.util.List;

public interface ExtractConfigMapper {
  List<ExtractConfigRow> getList(int tenantId);
}
