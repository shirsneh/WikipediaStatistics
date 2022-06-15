package cloud.wikipedia;

import cloud.wikipedia.model.MostActive;
import cloud.wikipedia.model.Utils;
import cloud.wikipedia.model.WikiObject;
import cloud.wikipedia.model.WikiUser;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RestService {
  private final HostInfo hostInfo;
  private final KafkaStreams streams;

  private static final Logger log = LoggerFactory.getLogger(RestService.class);
  private static final String baseUrl = "/wiki.stats/";
  private static final List<String> countTypes = Arrays.asList("new", "edit");

  RestService(HostInfo hostInfo, KafkaStreams streams) {
    this.hostInfo = hostInfo;
    this.streams = streams;
  }

  ReadOnlyKeyValueStore<String, Long> getCountsStore(String filter, String type) {
    return streams.store(
        StoreQueryParameters.fromNameAndType(
            filter + "-count-pages-" + type, QueryableStoreTypes.keyValueStore()));
  }

  ReadOnlyKeyValueStore<String, Long> getRevertCountStore(String filter) {
    return streams.store(
        StoreQueryParameters.fromNameAndType(
            filter + "-count-pages-revert", QueryableStoreTypes.keyValueStore()));
  }

  ReadOnlyKeyValueStore<String, MostActive<WikiObject>> getMostActivePagesStore(String filter) {
    return streams.store(
        StoreQueryParameters.fromNameAndType(
            filter + "-mostActivePages", QueryableStoreTypes.keyValueStore()));
  }

  ReadOnlyKeyValueStore<String, MostActive<WikiUser>> getMostActiveUsersStore(String filter) {
    return streams.store(
        StoreQueryParameters.fromNameAndType(
            filter + "-mostActiveUsers", QueryableStoreTypes.keyValueStore()));
  }

  ReadOnlyKeyValueStore<String, Long> getAllEventsStore() {
    return streams.store(
        StoreQueryParameters.fromNameAndType("all-events", QueryableStoreTypes.keyValueStore()));
  }

  boolean validateCountType(String type) {
    return countTypes.contains(type);
  }

  boolean isTimeFilter(String time) {
    return Utils.timeWindows.containsKey(time);
  }

  boolean validateFilter(String filter) {
    return Utils.timeWindows.containsKey(filter)
        || filter.equals("lang")
        || filter.equals("user-type");
  }

  void start() {
    Javalin app = Javalin.create().start(hostInfo.port());

    app.get(baseUrl + "count/:type/:filter", this::getCount);
    app.get(baseUrl + "count-revert/:filter", this::getCountRevert);
    app.get(baseUrl + "mostActiveUsers/:filter", this::mostActiveUsers);
    app.get(baseUrl + "mostActivePages/:filter", this::mostActivePages);
  }

  void getDuration(String time) {}

  void getCount(Context ctx) {
    String type = ctx.pathParam("type");
    String filter = ctx.pathParam("filter");

    if (!validateCountType(type) || !validateFilter(filter)) {
      ctx.status(404);
      return;
    }

    HashMap<String, Float> division = new HashMap<>();
    KeyValueIterator<String, Long> range = getCountsStore(filter, type).all();

    while (range.hasNext()) {
      KeyValue<String, Long> next = range.next();
      division.put(next.key, next.value.floatValue());
    }
    range.close();

    float sum = 0.0f;
    for (float f : division.values()) {
      sum += f;
    }

    HashMap<String, List<Float>> percentage = new HashMap<>();
    for (Map.Entry<String, Float> entry : division.entrySet()) {
      String key = entry.getKey();
      Float value = entry.getValue();
      percentage.put(key, List.of(value, (value / sum) * 100));
    }

    ctx.json(percentage);
  }

  void getCountRevert(Context ctx) {
    String filter = ctx.pathParam("filter");

    if (!validateFilter(filter)) {
      ctx.status(404);
      return;
    }

    HashMap<String, Float> division = new HashMap<>();
    KeyValueIterator<String, Long> range = getRevertCountStore(filter).all();

    while (range.hasNext()) {
      KeyValue<String, Long> next = range.next();
      division.put(next.key, next.value.floatValue());
    }
    range.close();

    float sum = 0.0f;
    for (float f : division.values()) {
      sum += f;
    }

    if (sum == 0) {
      ctx.json("No Revert actions recorded");
      return;
    }

    HashMap<String, List<Float>> percentage = new HashMap<>();
    for (Map.Entry<String, Float> entry : division.entrySet()) {
      String key = entry.getKey();
      Float value = entry.getValue();
      percentage.put(key, List.of(value, (value / sum) * 100));
    }

    ctx.json(percentage);
  }

  void mostActiveUsers(Context ctx) {
    String filter = ctx.pathParam("filter");

    if (!validateFilter(filter)) {
      ctx.status(404);
    }

    List<WikiUser> output = null;
    KeyValueIterator<String, MostActive<WikiUser>> it = getMostActiveUsersStore(filter).all();

    while (it.hasNext()) {
      KeyValue<String, MostActive<WikiUser>> obj = it.next();
      output = obj.value.toList();
    }

    it.close();
    assert output != null;
    ctx.json(output);
  }

  void mostActivePages(Context ctx) {
    String filter = ctx.pathParam("filter");

    if (!validateFilter(filter)) {
      ctx.status(404);
    }

    HashMap<String, List<WikiObject>> output = new HashMap<>();
    KeyValueIterator<String, MostActive<WikiObject>> it = getMostActivePagesStore(filter).all();

    while (it.hasNext()) {
      KeyValue<String, MostActive<WikiObject>> obj = it.next();
      output.put(obj.key, obj.value.toList());
    }

    it.close();
    assert output != null;
    ctx.json(output);
  }

  //  void getAllInRange(Context ctx) {
  //    List<Map<String, Object>> bpms = new ArrayList<>();
  //
  //    String from = ctx.pathParam("from");
  //    String to = ctx.pathParam("to");
  //
  //    Instant fromTime = Instant.ofEpochMilli(Long.parseLong(from));
  //    Instant toTime = Instant.ofEpochMilli(Long.parseLong(to));
  //
  //    KeyValueIterator<Windowed<String>, Long> range = getBpmStore().fetchAll(fromTime, toTime);
  //    while (range.hasNext()) {
  //      Map<String, Object> bpm = new HashMap<>();
  //      KeyValue<Windowed<String>, Long> next = range.next();
  //      String key = next.key.key();
  //      Window window = next.key.window();
  //      Long start = window.start();
  //      Long end = window.end();
  //      Long count = next.value;
  //      bpm.put("key", key);
  //      bpm.put("start", Instant.ofEpochMilli(start).toString());
  //      bpm.put("end", Instant.ofEpochMilli(end).toString());
  //      bpm.put("count", count);
  //      bpms.add(bpm);
  //    }
  //    // close the iterator to avoid memory leaks
  //    range.close();
  //    // return a JSON response
  //    ctx.json(bpms);
  //  }
  //
  //  void getRange(Context ctx) {
  //    List<Map<String, Object>> bpms = new ArrayList<>();
  //
  //    String key = ctx.pathParam("key");
  //    String from = ctx.pathParam("from");
  //    String to = ctx.pathParam("to");
  //
  //    Instant fromTime = Instant.ofEpochMilli(Long.valueOf(from));
  //    Instant toTime = Instant.ofEpochMilli(Long.valueOf(to));
  //
  //    WindowStoreIterator<Long> range = getBpmStore().fetch(key, fromTime, toTime);
  //    while (range.hasNext()) {
  //      Map<String, Object> bpm = new HashMap<>();
  //      KeyValue<Long, Long> next = range.next();
  //      Long timestamp = next.key;
  //      Long count = next.value;
  //      bpm.put("timestamp", Instant.ofEpochMilli(timestamp).toString());
  //      bpm.put("count", count);
  //      bpms.add(bpm);
  //    }
  //    // close the iterator to avoid memory leaks
  //    range.close();
  //    // return a JSON response
  //    ctx.json(bpms);
  //  }
}
