package com.magicalpipelines;

import com.magicalpipelines.model.MostActive;
import com.magicalpipelines.model.WikiActive;
import com.magicalpipelines.model.WikiEvent;
import com.magicalpipelines.model.WikiUser;
import com.magicalpipelines.serialization.json.JsonSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class WikipediaStatisticsTopology {

    public static List<Map.Entry<String, KStream<String, WikiEvent>>> viewStreamInTimeRange(
            String streamName, KStream<String, WikiEvent> stream) {
        // branch by hour, day, week and month
        Instant currDateTime = Instant.now();

        List<Map.Entry<String, KStream<String, WikiEvent>>> timeRangeStreams = new ArrayList<>();

        timeRangeStreams.add(
                Map.entry(
                        "month-" + streamName,
                        stream.filter(
                                (key, wikiEvent) -> ChronoUnit.DAYS.between(currDateTime, Instant.parse(wikiEvent.getDateTime()))
                                        <= 30)));

        timeRangeStreams.add(
                Map.entry(
                        "week-" + streamName,
                        stream.filter(
                                (key, wikiEvent) -> ChronoUnit.DAYS.between(currDateTime, Instant.parse(wikiEvent.getDateTime()))
                                        <= 7)));

        timeRangeStreams.add(
                Map.entry(
                        "day-" + streamName,
                        stream.filter(
                                (key, wikiEvent) -> ChronoUnit.DAYS.between(currDateTime, Instant.parse(wikiEvent.getDateTime()))
                                        <= 1)));

        timeRangeStreams.add(
                Map.entry(
                        "hour-" + streamName,
                        stream.filter(
                                (key, wikiEvent) -> ChronoUnit.HOURS.between(currDateTime, Instant.parse(wikiEvent.getDateTime()))
                                        <= 1)));

        return timeRangeStreams;
    }

    public static void countPages(String streamName, KStream<String, WikiEvent> Stream, String type) {
        String newStreamName = type.equals("new") ? "-countPagesCreated" : "-countPagesModified";

        KStream<String, WikiEvent> pagesStream =
                Stream.filter((key, wikiEvent) -> wikiEvent.getType().equals(type));

        KTable<String, Long> pagesCount =
                pagesStream
                        .groupByKey(Grouped.with(Serdes.String(), JsonSerdes.WikiEvent()))
                        .count(Materialized.as(streamName + newStreamName));
    }

    public static void mostActiveUsers(String streamName, KStream<String, WikiEvent> stream) {

        /* Transform to WikiUser records */
        KStream<String, WikiUser> _usersStream =
                stream.mapValues((wikiEvent) -> new WikiUser(wikiEvent));


        _usersStream.foreach(
                (key, user) -> {
                    System.out.println(key + ", "+ user.getName());
                });

        KGroupedStream<String, WikiUser> usersStream =
                _usersStream.groupByKey(Grouped.with(Serdes.String(), JsonSerdes.WikiUser()));


        Initializer<MostActive<WikiUser>> mostActiveInitializer = MostActive::new;

        /** The logic for aggregating high scores is implemented in the MostActive.add method */
        Aggregator<String, WikiUser, MostActive<WikiUser>> mostActiveAdder =
                (key, value, aggregate) -> aggregate.add(value);

        /** Perform the aggregation, and materialize the underlying state store for querying */
        KTable<String, MostActive<WikiUser>> mostActive =
                usersStream.aggregate(
                        mostActiveInitializer,
                        mostActiveAdder,
                        Materialized.<String, MostActive<WikiUser>, KeyValueStore<Bytes, byte[]>>
                                // give the state store an explicit name to make it available for interactive
                                // queries
                                        as(streamName + "-active-users")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(JsonSerdes.MostActiveUsers()));

        mostActive.toStream().to("mostActiveUsers");
    }

    public static void mostActivePages(String streamName, KStream<String, WikiEvent> stream) {

        /* Transform to WikiUser records */
        KStream<String, WikiActive> _usersStream =
                stream.mapValues((wikiEvent) -> new WikiUser(wikiEvent));

        KGroupedStream<String, WikiActive> usersStream =
                _usersStream.groupByKey(Grouped.with(Serdes.String(), JsonSerdes.WikiActive()));


        _usersStream.foreach(
                (key, page) -> {
                    System.out.println(key + "," + page.getName());
                });

        Initializer<MostActive<WikiActive>> mostActiveInitializer = MostActive::new;

        /** The logic for aggregating high scores is implemented in the MostActive.add method */
        Aggregator<String, WikiActive, MostActive<WikiActive>> mostActiveAdder =
                (key, value, aggregate) -> aggregate.add(value);

        /** Perform the aggregation, and materialize the underlying state store for querying */
        KTable<String, MostActive<WikiActive>> mostActive =
                usersStream.aggregate(
                        mostActiveInitializer,
                        mostActiveAdder,
                        Materialized.<String, MostActive<WikiActive>, KeyValueStore<Bytes, byte[]>>
                                // give the state store an explicit name to make it available for interactive
                                // queries
                                        as(streamName + "-active-pages")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(JsonSerdes.MostActivePages()));

        mostActive.toStream().to("mostActiveUsers");

    }

    public static void statefulOperations(
            List<Map.Entry<String, KStream<String, WikiEvent>>> timeRangedStreams) {

        // Count how many pages were created
        timeRangedStreams.forEach(
                (nameStreamPair) -> countPages(nameStreamPair.getKey(), nameStreamPair.getValue(), "new"));

        // Count how many pages were modified
        timeRangedStreams.forEach(
                (nameStreamPair) -> countPages(nameStreamPair.getKey(), nameStreamPair.getValue(), "edit"));

        // Sort *users* by most active
        timeRangedStreams.forEach(
                (nameStreamPair) -> mostActiveUsers(nameStreamPair.getKey(), nameStreamPair.getValue()));

        // Sort *pages* by most active
        timeRangedStreams.forEach(
                (nameStreamPair) -> mostActivePages(nameStreamPair.getKey(), nameStreamPair.getValue()));
    }

    public static Topology build() {

        // the builder is used to construct the topology
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, WikiEvent> wikiEvents =
                // register the last changes stream
                builder
                        .stream("wikipedia-events", Consumed.with(Serdes.String(), JsonSerdes.WikiEvent()))
                        .selectKey((key, wikiEvent) -> "");

        wikiEvents.foreach((key, value) -> System.out.println(key + ": " + value));

        wikiEvents.print(Printed.<String, WikiEvent>toSysOut().withLabel("WikiEvent"));

        // all records stream
        var allTimeRangedStreams = viewStreamInTimeRange("all", wikiEvents);
        statefulOperations(allTimeRangedStreams);

        // key by language
        KStream<String, WikiEvent> langStreams =
                wikiEvents.selectKey((key, wikiEvent) -> wikiEvent.getLanguage());

        var langTimeRangedStreams = viewStreamInTimeRange("per-language", langStreams);
        statefulOperations(langTimeRangedStreams);

        // key by isBot
        KStream<String, WikiEvent> userTypeStreams =
                wikiEvents.selectKey((key, wikiEvent) -> wikiEvent.getUserType());

        var userTypeTimeRangedStreams = viewStreamInTimeRange("per-userType", userTypeStreams);
        statefulOperations(userTypeTimeRangedStreams);


        return builder.build();
    }
}