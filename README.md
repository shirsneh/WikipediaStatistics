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
The Kafka Streams is a Gradle project, the most convenient way to open it is using IntellIJ and run `main` from there.

If you don't own IntellIJ you can run it using CLI:
```
./gradlew
./gradlew run
```

### Kafka Producer
ONE TIME ONLY - Install requirements defined in `requirements.txt`:

```bash
python3 -m pip install -r requirements.txt
```

Run script:
```bash
python3 wikipedia-statistics/src/main/python/kafka_Producer.py --bootstrap-server localhost:29092 --topic-name wikipedia-events --events-to-produce 100
```
You should see the producer being killed after 100 events.

## Queries
Note: Most recommended to run with Postman to prettify JSON - we aren't really good with frontedn and JS :(

### Parameters
| Parameter   | Applies to   | Values                  |
| ----------- | ----------   | -----------             |
| filter      | all requests | all,lang, user-type, hour, week, month, year |
| action      | all requests | count,count-revert, mostActiveUsers, mostActivePages |
| type        | count requests | edit, create

### Simple Counts
`http://localhost:9000/wiki.stats/count/{type}/{filter}`

for example, quering counts of new pages by language:
```
http://localhost:9000/wiki.stats/count/edit/lang
```

### Most Active Users
`http://localhost:9000/wiki.stats/mostActiveUsers/{filter}`

for example, quering top users by language:
```
http://localhost:9000/wiki.stats/mostActiveUsers/lang
```

### Most Active Pages
`http://localhost:9000/wiki.stats/mostActivePages/{filter}`

for example, quering top pages by language:
```
http://localhost:9000/wiki.stats/mostActivePages/lang
```

## Produce Test Data
Wikipedia doesn't always have all types of events, so we created some test json events.

## References
- We got a lot of help from the book `Mastering Kafka streams and ksqldb`, especially from chapters 4,5.
- We used [this](https://towardsdatascience.com/introduction-to-apache-kafka-with-wikipedias-eventstreams-service-d06d4628e8d9) tutorial to create the Kafka Producer in Python






