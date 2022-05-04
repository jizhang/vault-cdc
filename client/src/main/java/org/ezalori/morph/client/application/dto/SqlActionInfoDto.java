package org.ezalori.morph.client.application.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by hey on 22/08/2017.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SqlActionInfoDto extends ActionInfoDto {

  private List<String> actionSql;

  public SqlActionInfoDto(String sourceRdsName, String sourceSchema, String sourceTable,
      long binlogTime, List<String> actionSql) {
    super(sourceRdsName, sourceSchema, sourceTable, binlogTime);
    this.actionSql = actionSql;
  }
}
