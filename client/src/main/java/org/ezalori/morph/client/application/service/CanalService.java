package org.ezalori.morph.client.application.service;

import java.sql.SQLException;
import org.ezalori.morph.client.application.dto.ActionDto;

/**
 * Created by hey on 10/06/2018.
 */
public interface CanalService {

  /**
   * connect the canal-server with single connector.
   */
  void connect();


  /**
   * connect the canal-server with cluster.
   */
  void connectCluster();

  /**
   * acquire message from canal-server and parse into Collections of ActionInfoDto.
   *
   * @return batch of action info
   */
  ActionDto messageToSql() throws SQLException;

  /**
   * notify canal-server after parsing was done.
   *
   * @param batchId 　unique id of acquiring.
   */
  void ack(long batchId);

  /**
   * rollback to the last ack position.
   */
  void rollback();

}
