package org.ezalori.morph.client.domain.parser;

import com.alibaba.otter.canal.protocol.CanalEntry.Header;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by hey on 01/02/2018.
 */
public interface Parser {

  List<String> getSqlFromEntry(Header header, RowChange rowChange) throws SQLException;

}
