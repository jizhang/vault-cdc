package com.shzhangji.vault.cdc.targettable;

import java.io.Serializable;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TargetRowSinkOptions implements Serializable {
  int instanceId;
  String url;
  String username;
  String password;
  int maxRows;
  long intervalMillis;
  int maxRetries;
  int queryTimeoutSecs;
}
