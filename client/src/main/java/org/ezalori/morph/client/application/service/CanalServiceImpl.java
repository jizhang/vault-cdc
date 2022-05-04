package org.ezalori.morph.client.application.service;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.EntryType;
import com.alibaba.otter.canal.protocol.CanalEntry.Header;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.alibaba.otter.canal.protocol.Message;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ezalori.morph.client.application.dto.ActionDto;
import org.ezalori.morph.client.application.dto.ActionInfoDto;
import org.ezalori.morph.client.application.dto.SqlActionInfoDto;
import org.ezalori.morph.client.domain.mysql.MysqlMapping;
import org.ezalori.morph.client.domain.parser.MysqlParser;
import org.ezalori.morph.client.domain.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CanalServiceImpl implements CanalService {
  private static Logger logger = LoggerFactory.getLogger(CanalServiceImpl.class);

  private static final String CANAL_SERVER_ADDRESS = "canal.server.address";

  private static final String CANAL_SERVER_PORT = "canal.server.port";

  private static final String CANAL_SERVER_ZKSERVER = "canal.server.zkserver";

  private static final String CANAL_SERVER_DESTINATION = "canal.server.destination";

  private static final String CANAL_SERVER_USERNAME = "canal.server.username";

  private static final String CANAL_SERVER_PASSWORD = "canal.server.password";

  private static final String CANAL_SERVER_BATCHSIZE = "canal.server.batchSize";

  private static final String CANAL_SERVER_RDSNAME = "canal.server.rdsname";

  private CanalConnector connector;

  private int batchSize;

  private String address;

  private int port;

  private String zkserver;

  private String destination;

  private String username;

  private String password;

  private String rdsName;

  public CanalServiceImpl() {

  }

  public CanalServiceImpl(String configFilePath) throws IOException {
  }

  @Override
  public void connect() {
    this.connector = CanalConnectors.newSingleConnector(
      new InetSocketAddress(this.address, port),
      destination,
      username,
      password);
    connector.connect();
    connector.rollback();
  }

  @Override
  public void connectCluster() {
    this.connector = CanalConnectors.newClusterConnector(zkserver, destination, "", "");
    connector.connect();
    connector.rollback();
  }

  /**
   * Acquire messages from canal-server with the batchSize.
   *
   * @return message
   */
  private Message messageAcquire() {
    return this.connector.getWithoutAck(batchSize);
  }

  @Override
  public ActionDto messageToSql() throws SQLException {
    Message message = messageAcquire();
    long batchId = message.getId();
    List<Entry> entrys = message.getEntries();

    if (batchId != -1 && entrys.size() != 0) {
      List<ActionInfoDto> actionList = getSqlFromEntries(entrys);
      return new ActionDto(actionList, batchId);
    }
    return null;
  }

  @Override
  public void ack(long batchId) {
    connector.ack(batchId);
  }

  @Override
  public void rollback() {
    connector.rollback();
  }

  /**
   * Parsing entries into Collections of ActionInfoDto.
   *
   * @param entries canalEntry.Entry
   * @return batch of action info
   */
  private List<ActionInfoDto> getSqlFromEntries(List<Entry> entries)
      throws SQLException {

    List<ActionInfoDto> actionList = new ArrayList<ActionInfoDto>();

    for (Entry entry : entries) {
      List<String> upsertSql = null;
      MysqlMapping mysqlMapping = null;

      if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN
          || entry.getEntryType() == EntryType.TRANSACTIONEND) {
        continue;
      }

      RowChange rowChange = null;

      try {
        rowChange = RowChange.parseFrom(entry.getStoreValue());
      } catch (Exception e) {
        throw new RuntimeException(
          "ERROR ## parser of eromanga-event has an error , data:" + entry.toString(), e
        );
      }

      Header header = entry.getHeader();

      CanalServiceImpl.ParserAndMapping pam = selectProperParser(header, mysqlMapping, rowChange);
      Parser parser = pam.getParser();
      mysqlMapping = pam.getMysqlMapping();

      if (parser != null) {
        upsertSql = parser.getSqlFromEntry(header, rowChange);

        if (upsertSql == null) {
          return null;
        }
      }

      if (mysqlMapping != null) {
        SqlActionInfoDto actionInfoDto = new SqlActionInfoDto(
            this.rdsName,
            mysqlMapping.getSourceSchema(),
            mysqlMapping.getSourceTable(),
            header.getExecuteTime(),
            upsertSql
        );

        actionList.add(actionInfoDto);
      }
    }
    return actionList;
  }

  private ParserAndMapping selectProperParser(Header header,
      MysqlMapping mysqlMapping, RowChange rowChange) {
    String schema = "";
    String table = "";
    Parser parser = null;
    // real-time binlogs
    if ("retl_buffer".equals(header.getTableName()) == false) {
      //TODO query for mapping
      if (mysqlMapping != null) {
        parser = new MysqlParser(mysqlMapping);
      }
    } else {
      // add the history data
      //TODO append history data
    }

    return new ParserAndMapping(mysqlMapping, parser);

  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  private static class ParserAndMapping {
    private MysqlMapping mysqlMapping;
    private Parser parser;
  }

}
