package cloud.wikipedia;

import cloud.wikipedia.model.*;
import cloud.wikipedia.serialization.json.JsonSerdes;
import java.time.Duration;
import java.time.Instant;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WikiStatsTopology {
  private static final Logger log = LoggerFactory.getLogger(WikiStatsTopology.class);

  public static Topology build(String streamName) {
    StreamsBuilder builder = new StreamsBuilder();

    Consumed<String, WikiEvent> eventsConsumerOptions =
        Consumed.with(Serdes.String(), JsonSerdes.WikiEvent())
            .withTimestampExtractor(new EventTimestampExtractor());

    KStream<String, WikiEvent> wikiEvents = builder.stream(streamName, eventsConsumerOptions);

    /* Time based statistics */
    Utils.timeWindows.forEach(
        (name, duration) ->
            countPagesSeperately(
                name + "-count-pages",
                wikiEvents.filter(
                    (key, value) ->
                        Duration.between(Instant.parse(value.getDateTime()), Instant.now())
                                .compareTo(duration)
                            <= 0)));

    Utils.timeWindows.forEach(
        (name, duration) ->
            countPages(
                name,
                wikiEvents.filter(
                    (key, value) ->
                        value.isRevert()
                            && Duration.between(Instant.parse(value.getDateTime()), Instant.now())
                                    .compareTo(duration)
                                <= 0)));

    Utils.timeWindows.forEach(
        (name, duration) ->
            countMostActiveUsers(
                name,
                wikiEvents.filter(
                    (key, value) ->
                        Duration.between(Instant.parse(value.getDateTime()), Instant.now())
                                .compareTo(duration)
                            <= 0)));

    Utils.timeWindows.forEach(
        (name, duration) ->
            countMostActivePages(
                name,
                wikiEvents.filter(
                    (key, value) ->
                        Duration.between(Instant.parse(value.getDateTime()), Instant.now())
                                .compareTo(duration)
                            <= 0)));

    /* User type based statistics */
    KStream<String, WikiEvent> userType = wikiEvents.selectKey((key, value) -> value.getUserType());
    allActions(userType, "user-type");

    /* Language based statistics */
    KStream<String, WikiEvent> lang = wikiEvents.selectKey((key, value) -> value.getLanguage());
    allActions(lang, "lang");

    return builder.build();
  }

  public static void allActions(KStream<String, WikiEvent> stream, String filter) {
    countPagesSeperately(filter + "-count-pages", stream);
    countPages(filter + "-count-pages-revert", stream.filter((key, value) -> value.isRevert()));
    countMostActivePages(filter, stream);
    countMostActiveUsers(filter, stream);
  }

  /**
   * Counts pages for each key in topic (edit/new pages are divided by key)
   *
   * @param baseStreamName The stream name so far [time-filter-count]
   * @param stream The stream of wiki events
   */
  public static void countPages(String baseStreamName, KStream<String, WikiEvent> stream) {
    KTable<String, Long> eventsCounts =
        stream
            .groupByKey(Grouped.with(Serdes.String(), JsonSerdes.WikiEvent()))
            .count(Materialized.as(baseStreamName));
  }

  public static void countPagesSeperately(
      String baseStreamName, KStream<String, WikiEvent> stream) {
    countPages(
        baseStreamName + "-edit", stream.filter((key, value) -> value.getType().equals("edit")));
    countPages(
        baseStreamName + "-new", stream.filter((key, value) -> value.getType().equals("new")));
  }

  public static void countMostActiveUsers(
      String baseStreamName, KStream<String, WikiEvent> stream) {
    KStream<String, WikiUser> convertedStream = stream.mapValues(value -> new WikiUser(value));

    KGroupedStream<String, WikiUser> groupedStream =
        convertedStream.groupByKey(Grouped.with(Serdes.String(), JsonSerdes.WikiUser()));

    Initializer<MostActive<WikiUser>> mostActiveInitializer = MostActive::new;

    /* The logic for aggregating high scores is implemented in the HighScores.add method */
    Aggregator<String, WikiUser, MostActive<WikiUser>> mostActiveAdder =
        (key, value, aggregate) -> aggregate.add(value);

    /* Perform the aggregation, and materialize the underlying state store for querying */
    KTable<String, MostActive<WikiUser>> mostActive =
        groupedStream.aggregate(
            mostActiveInitializer,
            mostActiveAdder,
            Materialized.<String, MostActive<WikiUser>, KeyValueStore<Bytes, byte[]>>
                // give the state store an explicit name to make it available for interactive
                // queries
                as(baseStreamName + WikiUser.getStreamName())
                .withKeySerde(Serdes.String())
                .withValueSerde(JsonSerdes.MostActiveUsers()));

    mostActive.toStream().foreach((key, value) -> log.debug("hi"));
  }

  public static void countMostActivePages(
      String baseStreamName, KStream<String, WikiEvent> stream) {

    KGroupedStream<String, WikiObject> convertedStream =
        stream
            .mapValues(WikiObject::new)
            .groupByKey(Grouped.with(Serdes.String(), JsonSerdes.WikiObject()));

    Initializer<MostActive<WikiObject>> mostActiveInitializer = MostActive::new;

    /* The logic for aggregating high scores is implemented in the HighScores.add method */
    Aggregator<String, WikiObject, MostActive<WikiObject>> mostActiveAdder =
        (key, value, aggregate) -> aggregate.add(value);

    /* Perform the aggregation, and materialize the underlying state store for querying */
    KTable<String, MostActive<WikiObject>> mostActive =
        convertedStream.aggregate(
            mostActiveInitializer,
            mostActiveAdder,
            Materialized.<String, MostActive<WikiObject>, KeyValueStore<Bytes, byte[]>>
                // give the state store an explicit name to make it available for interactive
                // queries
                as(baseStreamName + WikiObject.getStreamName())
                .withKeySerde(Serdes.String())
                .withValueSerde(JsonSerdes.MostActivePages()));
  }
}
