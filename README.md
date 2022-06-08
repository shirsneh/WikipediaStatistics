# WikipediaStats
Event driven system to analyze statistics of Wikipedia pages using Kafka.

# Setup
In order to setup a Kafka cluster perform the following actions:

## Docker
Run the containers defined in the docker-compose file:

```bash
sudo docker-compose up
```

And wait for them to be up and running.

## Kafka Streams
Run via gradlew, or even better, open it up in Intellij, and select gradle config.

## Kafka Producer
Install requirements defined in `requirements.txt`:

```bash
python3 -m pip install -r requirements.txt
```

Run script:
```bash
python3 src/producer/kafka_producer.py --bootstrap-server localhost:29092 --topic-name wikipedia-events --events-to-produce 100
```
You should see the producer being killed after 100 events.


