#############################################################
##### First argument is the path to the kafka directory #####
############################################################

# Start ZooKeeper service
$1/bin/zookeeper-server-start.sh $1/config/zookeeper.properties &

# Run all brokers in parallel
$1/bin/kafka-server-start.sh $1/config/server.properties &

# Create topic
$1/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic wikipedia-events &

# Run producer
python3 src/producer/kafka_producer.py &

# #
# $1/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic wikipedia-events --from-beginning &
