package org.ezalori.morph.client.application.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by hey on 24/08/2017.
 */
public class ActionServiceImpl implements ActionService {

  private static Logger logger = LoggerFactory.getLogger(ActionServiceImpl.class);

  @Override
  public void batchIdAck(List<Long> batchIdList, CanalService canalService) {
    if (batchIdList != null && canalService != null && batchIdList.size() > 0) {
      batchIdList.forEach(canalService::ack);
      batchIdList.clear();
    }
  }
}
