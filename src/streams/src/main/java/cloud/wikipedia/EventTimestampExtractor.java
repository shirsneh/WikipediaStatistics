package cloud.wikipedia;

import cloud.wikipedia.model.WikiEvent;
import java.time.Instant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

/** This class allows us to use event-time semantics for purchase streams */
public class EventTimestampExtractor implements TimestampExtractor {

  @Override
  public long extract(ConsumerRecord<Object, Object> record, long partitionTime) {
    WikiEvent measurement = (WikiEvent) record.value();
    if (measurement != null && measurement.getDateTime() != null) {
      String timestamp = measurement.getDateTime();
      // System.out.println("Extracting timestamp: " + timestamp);
      return Instant.parse(timestamp).toEpochMilli();
    }
    // fallback to stream time
    return partitionTime;
  }
}
