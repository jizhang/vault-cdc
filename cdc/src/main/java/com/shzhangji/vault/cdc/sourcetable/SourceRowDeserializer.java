package com.shzhangji.vault.cdc.sourcetable;

import com.shzhangji.vault.etl.sourcetable.RowKind;
import com.shzhangji.vault.etl.sourcetable.SourceRow;
import com.shzhangji.vault.etl.sourcetable.SourceTable;
import com.ververica.cdc.debezium.DebeziumDeserializationSchema;
import io.debezium.data.Envelope;
import io.debezium.time.Date;
import io.debezium.time.MicroTime;
import io.debezium.time.Timestamp;
import io.debezium.time.ZonedTimestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.util.Collector;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

@RequiredArgsConstructor
public class SourceRowDeserializer implements DebeziumDeserializationSchema<SourceRow> {
  private static final DateTimeFormatter dfDateTime =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final DateTimeFormatter dfDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private final int instanceId;

  @Override
  public void deserialize(SourceRecord record, Collector<SourceRow> out) throws Exception {
    var op = Envelope.operationFor(record);
    var rowKind = op == Envelope.Operation.DELETE ? RowKind.DELETE : RowKind.REPLACE;

    var value = (Struct) record.value();
    var source = value.getStruct(Envelope.FieldName.SOURCE);
    var database = source.getString("db");
    var table = source.getString("table");

    var row = rowKind == RowKind.DELETE
        ? value.getStruct(Envelope.FieldName.BEFORE)
        : value.getStruct(Envelope.FieldName.AFTER);
    var columns = new HashMap<String, String>();
    for (var field : row.schema().fields()) {
      var fieldValue = row.get(field);
      var columnValue = convertValue(fieldValue, field.schema());
      columns.put(field.name().toLowerCase(), columnValue);
    }

    var sourceTable = new SourceTable(instanceId, database, table);
    out.collect(new SourceRow(sourceTable, columns, rowKind));
  }

  @Override
  public TypeInformation<SourceRow> getProducedType() {
    return TypeInformation.of(SourceRow.class);
  }

  protected String convertValue(Object value, Schema schema) {
    if (value == null) {
      return null;
    }

    if (schema.name() == null) {
      return value.toString();
    }

    switch (schema.name()) {
      case ZonedTimestamp.SCHEMA_NAME:
        return Instant.parse((String) value)
          .atZone(ZoneId.systemDefault())
          .toLocalDateTime()
          .format(dfDateTime);

      case Timestamp.SCHEMA_NAME:
        return Instant.ofEpochMilli((Long) value)
          .atOffset(ZoneOffset.UTC)
          .toLocalDateTime()
          .format(dfDateTime);

      case Date.SCHEMA_NAME:
        return LocalDate.ofEpochDay((Integer) value)
          .format(dfDate);

      case MicroTime.SCHEMA_NAME:
        var duration = Duration.ofMillis((Long) value / 1000);
        return String.format("%02d:%02d:%02d",
            duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());

      default:
        return value.toString();
    }
  }
}
