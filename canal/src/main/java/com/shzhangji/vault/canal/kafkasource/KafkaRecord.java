package com.shzhangji.vault.canal.kafkasource;

import lombok.Value;

@Value
public class KafkaRecord {
  String topic;
  byte[] value;
}
