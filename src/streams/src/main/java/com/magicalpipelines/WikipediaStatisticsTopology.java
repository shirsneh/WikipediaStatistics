package com.magicalpipelines;

import com.magicalpipelines.model.WikiActive;
import com.magicalpipelines.model.WikiEvent;
import com.magicalpipelines.model.MostActive;

import com.magicalpipelines.model.WikiUser;
import com.magicalpipelines.serialization.json.JsonSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class WikipediaStatisticsTopology {

    public static List<Map.Entry<String, KStream<String, WikiEvent>>> viewStreamInTimeRange(
            String streamName, KStream<String, WikiEvent> stream) {
        // branch by hour, day, week and month
        LocalDate todayDate = LocalDate.now();
        LocalTime currTime = LocalTime.now();


        List<Map.Entry<String, KStream<String, WikiEvent>>> timeRangeStreams = new ArrayList<>();

        timeRangeStreams.add(
                Map.entry(
                        "month-" + streamName,
                        stream.filter(
                                (key, wikiEvent) -> {
                                    return ChronoUnit.DAYS.between(todayDate, LocalDate.parse(wikiEvent.getDate()))
                                            <= 30;
                                })));

        timeRangeStreams.add(
                Map.entry(
                        "week-" + streamName,
                        stream.filter(
                                (key, wikiEvent) -> {
                                    return ChronoUnit.DAYS.between(todayDate, LocalDate.parse(wikiEvent.getDate()))
                                            <= 7;
                                })));

        timeRangeStreams.add(
                Map.entry(
                        "day-" + streamName,
                        stream.filter(
                                (key, wikiEvent) -> {
                                    return ChronoUnit.DAYS.between(todayDate, LocalDate.parse(wikiEvent.getDate()))
                                            <= 1;
                                })));

        timeRangeStreams.add(
                Map.entry(
                        "hour-" + streamName,
                        stream.filter(
                                (key, wikiEvent) -> {
                                    return ChronoUnit.HOURS.between(currTime, LocalTime.parse(wikiEvent.getTime()))
                                            <= 1;
                                })));

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

        pagesCount.toStream();
    }

//  public static void countPagesCreated(String streamName, KStream<String, WikiEvent> Stream) {
//
//    KStream<String, WikiEvent> createdPagesStream =
//        Stream.filter((key, wikiEvent) -> wikiEvent.getType().equals("new"));
//
//    KTable<String, Long> createdPagesCount =
//        createdPagesStream
//            .groupByKey(Grouped.with(Serdes.String(), JsonSerdes.WikiEvent()))
//            .count(Materialized.as(streamName + "-countPagesCreated"));
//
//    createdPagesCount
//        .toStream();
//        // .foreach(
//        //     (key, value) -> {
//        //       System.out.println("(DSL) ZELLO, " + value);
//        //     });
//  }
//
//  public static void countPagesModified(String streamName, KStream<String, WikiEvent> Stream) {
//
//    KStream<String, WikiEvent> modifiedPagesStream =
//        Stream.filter((key, wikiEvent) -> wikiEvent.getType().equals("edit"));
//
//    KTable<String, Long> modifiedPagesCount =
//        modifiedPagesStream
//            .groupByKey(Grouped.with(Serdes.String(), JsonSerdes.WikiEvent()))
//            .count(Materialized.as(streamName + "-countPagesModified"));
//  }

    public static void mostActiveUsers(String streamName, KStream<String, WikiEvent> stream) {

        // System.out.println("YOLO");
        // JsonSerializer<SortedWikiUsers> serializer = new JsonSerializer<>();
        // serializer.serialize("WikiEvents", new SortedWikiUsers());

        /* Transform to WikiUser records */
        KStream<String, WikiUser> _usersStream =
                stream.mapValues((wikiEvent) -> new WikiUser(wikiEvent));

//    _usersStream.foreach(
//        (key, user) -> {
//          System.out.println("2EZ " + user.getName());
//        });

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

//        mostActive.toStream().to("most-active-users");
    }

    public static void mostActivePages(String streamName, KStream<String, WikiEvent> stream) {

        // System.out.println("YOLO");
        // JsonSerializer<SortedWikiUsers> serializer = new JsonSerializer<>();
        // serializer.serialize("WikiEvents", new SortedWikiUsers());

        /* Transform to WikiUser records */
        KStream<String, WikiActive> _usersStream =
                stream.mapValues((wikiEvent) -> new WikiUser(wikiEvent));

//    _usersStream.foreach(
//        (key, user) -> {
//          System.out.println("2EZ " + user.getName());
//        });

        KGroupedStream<String, WikiActive> usersStream =
                _usersStream.groupByKey(Grouped.with(Serdes.String(), JsonSerdes.WikiActive()));


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

//        mostActive.toStream().to("most-active-pages");
    }

    public static void statefulOperations(
            List<Map.Entry<String, KStream<String, WikiEvent>>> timeRangedStreams) {
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
        // SortedWikiUsers wikiusers = new SortedWikiUsers();
        // wikiusers.add("name", new WikiUser("name", true));

        // JsonSerializer json = new JsonSerializer();
        // json.serialize("Bla", wikiusers);

        // try {
        //   TimeUnit.MINUTES.sleep(1);
        // } catch (InterruptedException e) {
        //   // TODO Auto-generated catch block
        //   e.printStackTrace();
        // }

        // the builder is used to construct the topology
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, WikiEvent> wikiEvents =
                // register the last changes stream
                builder
                .stream("wikipedia-events", Consumed.with(Serdes.String(), JsonSerdes.WikiEvent()))
                .selectKey((key, wikiEvent) -> "");

        wikiEvents.print(Printed.<String, WikiEvent>toSysOut().withLabel("WikiEvent"));

//        wikiEvents.foreach(
//                (key, value) -> {
//                    System.out.println("(DSL) Hello, " + value);
//                });

        // all records stream
        var allTimeRangedStreams = viewStreamInTimeRange("all", wikiEvents);
        statefulOperations(allTimeRangedStreams);

        // key by language
        KStream<String, WikiEvent> langStreams =
                wikiEvents.selectKey((key, wikiEvent) -> wikiEvent.getLang());

        var langTimeRangedStreams = viewStreamInTimeRange("per-language", langStreams);
        statefulOperations(langTimeRangedStreams);

        // key by isBot
        KStream<String, WikiEvent> userTypeStreams =
                wikiEvents.selectKey((key, wikiEvent) -> wikiEvent.getIsBot().toString());

        var userTypeTimeRangedStreams = viewStreamInTimeRange("per-userType", userTypeStreams);
        statefulOperations(userTypeTimeRangedStreams);

        // collect all streams
        // List<Map.Entry<String, KStream<String, WikiEvent>>> timeRangedStreams =
        //     Stream.of(allTimeRangedStreams, langTimeRangedStreams, userTypeTimeRangedStreams)
        //         .flatMap(Collection::stream)
        //         .collect(Collectors.toList());

        // start collecting statistics

        // Count how many pages were created

        // Sort *pages* by most active
        //        timeRangedStreams.forEach(
        //                (nameStreamPair)->mostActivePages(nameStreamPair.getKey(),
        // nameStreamPair.getValue()));

        // Count how many revert action were committed

        return builder.build();
    }
}
