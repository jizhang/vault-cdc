package com.shzhangji.vault.streaming;

import com.shzhangji.vault.canal.ExtractCanalBase;
import com.shzhangji.vault.canal.ExtractCanalOptions;
import com.shzhangji.vault.etl.extractconfig.DbInstanceId;
import com.shzhangji.vault.etl.extractconfig.TenantId;
import java.util.HashMap;

public class ExtractCanal {
  public static void main(String[] args) throws Exception {
    var topicInstances = new HashMap<String, Integer>();
    topicInstances.put("canal.vault", DbInstanceId.VAULT);

    var options = ExtractCanalOptions.builder()
        .tenantId(TenantId.VAULT)
        .groupId("bi.vault.extract_canal")
        .jobName("extract-canal")
        .topicInstances(topicInstances)
        .build();

    var job = new ExtractCanalBase(options);
    job.run();
  }
}
