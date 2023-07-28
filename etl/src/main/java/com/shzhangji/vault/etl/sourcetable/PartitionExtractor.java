package com.shzhangji.vault.etl.sourcetable;

import java.util.Map;

public interface PartitionExtractor {
  Map<String, String> extract(SourceTable sourceTable, Map<String, String> columns);
}
