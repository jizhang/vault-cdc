package com.shzhangji.vault.canal.kafkasource;

import com.shzhangji.vault.etl.sourcetable.SourceTable;
import lombok.Value;

@Value
public class SourceMessage {
  SourceTable sourceTable;
  byte[] rowChangeData;
}
