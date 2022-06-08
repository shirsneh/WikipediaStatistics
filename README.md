# WikipediaStats
Event driven system to analyze statistics of Wikipedia pages using Kafka.

## Setup
In order to setup a Kafka cluster perform the following actions:

### Docker
Run the containers defined in the docker-compose file:

```bash
sudo docker-compose up
```

And wait for them to be up and running.

### Kafka Streams
Run via gradlew, or even better, open it up in Intellij, and select gradle config.

### Kafka Producer
Install requirements defined in `requirements.txt`:

```bash
python3 -m pip install -r requirements.txt
```

Run script:
```bash
python3 wikipedia-statistics/src/main/python/kafka_Producer.py --bootstrap-server localhost:29092 --topic-name wikipedia-events --events-to-produce 100
```
You should see the producer being killed after 100 events.

### Web Server Displaying results
Go to the right location:

```bash
 cd wikipedia-statistics/src/main/js/my_app
```

Install dependencies

```bash
 npm install
```

Start the web-server

```bash
 npm start
```

## Useful Resources
Throughout our work on this project, we found the following to be extremely helpful:
- The GitHub of Mitch Seymour: https://github.com/mitch-seymour/mastering-kafka-streams-and-ksqldb.
- Wikitech Event Platform/EventStreams : https://wikitech.wikimedia.org/wiki/Event_Platform/EventStreams.
- Introduction to Apache Kafka with Wikipedia’s EventStreams service article: https://towardsdatascience.com/introduction-to-apache-kafka-with-wikipedias-eventstreams-service-d06d4628e8d9
- Tutorial: Write a kafka streams application: https://kafka.apache.org/31/documentation/streams/tutorial.
- The Chakra-UI Open-Source template: https://horizon-ui.com.




