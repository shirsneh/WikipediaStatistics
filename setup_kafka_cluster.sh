#############################################################
##### First argument is the path to the kafka directory #####
#############################################################

KAFKA_EVENTS_TOPIC="wikipedia-events"
KAFKA_PUBLISH_TOPIC="wikipedia-stats"

# Start ZooKeeper service
"$1"/bin/zookeeper-server-start.sh "$1"/config/zookeeper.properties &

echo "***************** ZooKeeper executed! *****************"
sleep 3

# Run all brokers in parallel
"$1"/bin/kafka-server-start.sh ./config/server1.properties &

echo "***************** Started Brokers *****************"
sleep 3

# Create topic
"$1"/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic $KAFKA_EVENTS_TOPIC &

echo "***************** Created Topic *****************"
sleep 3

# Run producer
./venv/bin/python3 src/producer/kafka_producer.py --bootstrap_server localhost:9092 --topic_name $KAFKA_EVENTS_TOPIC --events_to_produce 1000 &

echo "***************** Created Topic *****************"
sleep 3

# #
# "$1"/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic wikipedia-events --from-beginning &
