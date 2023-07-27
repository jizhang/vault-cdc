package com.shzhangji.vault.cdc.sourcetable;

import com.shzhangji.vault.cdc.sourcetable.SourceTable;
import java.util.Map;

public interface PartitionExtractor {
  Map<String, String> extract(SourceTable sourceTable, Map<String, String> columns);
}
