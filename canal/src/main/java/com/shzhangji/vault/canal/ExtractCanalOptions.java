package com.shzhangji.vault.canal;

import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ExtractCanalOptions {
  int tenantId;
  String groupId;
  String jobName;
  Map<String, Integer> topicInstances;
}
