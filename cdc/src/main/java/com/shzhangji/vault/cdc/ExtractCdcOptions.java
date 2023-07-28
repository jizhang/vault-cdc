package com.shzhangji.vault.cdc;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ExtractCdcOptions {
  int tenantId;
  String jobName;
  List<SourceInstance> sourceInstanceList;
}
