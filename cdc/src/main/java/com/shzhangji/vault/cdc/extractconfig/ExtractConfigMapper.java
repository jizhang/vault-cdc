package com.shzhangji.vault.cdc.extractconfig;

import java.util.List;

public interface ExtractConfigMapper {
  List<ExtractConfigRow> getList(int tenantId);
}
