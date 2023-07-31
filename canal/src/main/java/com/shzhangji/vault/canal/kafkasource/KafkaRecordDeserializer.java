package com.shzhangji.vault.canal.kafkasource;

import java.io.IOException;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public class KafkaRecordDeserializer implements KafkaRecordDeserializationSchema<KafkaRecord> {
  @Override
  public void deserialize(ConsumerRecord<byte[], byte[]> record, Collector<KafkaRecord> out)
      throws IOException {

    out.collect(new KafkaRecord(record.topic(), record.value()));
  }

  @Override
  public TypeInformation<KafkaRecord> getProducedType() {
    return TypeInformation.of(KafkaRecord.class);
  }
}
