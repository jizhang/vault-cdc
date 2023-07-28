package com.shzhangji.vault.streaming;

import com.shzhangji.vault.cdc.ExtractCdcBase;
import com.shzhangji.vault.cdc.ExtractCdcOptions;
import com.shzhangji.vault.cdc.SourceInstance;
import com.shzhangji.vault.etl.extractconfig.DbInstanceId;
import com.shzhangji.vault.etl.extractconfig.TenantId;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VaultExtractCdc {
  public static void main(String[] args) throws Exception {
    var vaultInstance = new SourceInstance(
        DbInstanceId.VAULT,
        List.of("vault"),
        List.of("vault\\..+"));

    var options = ExtractCdcOptions.builder()
        .tenantId(TenantId.VAULT)
        .jobName("vault-extract-cdc")
        .sourceInstanceList(List.of(vaultInstance))
        .build();

    var job = new ExtractCdcBase(options);
    job.run();
  }
}
