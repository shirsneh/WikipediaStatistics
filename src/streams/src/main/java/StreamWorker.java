import org.apache.kafka.streams.KafkaStreams;

public class StreamWorker {
    public static void main(final String[] args) {
        // Parse arguments, and if not valid set a defuault server
        final String bootstrapServers = args.length > 0 ? args[0] : "localhost:9092";

        // Configure the Streams application.
        final Properties streamsConfiguration = getStreamsConfiguration(bootstrapServers);

        // Define the processing topology of the Streams application.
        final StreamsBuilder builder = new StreamsBuilder();
        createWordCountStream(builder);
        final KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);

    }

    private static void createStatisticsStreams(final StreamBuilder builder) {

    }
}