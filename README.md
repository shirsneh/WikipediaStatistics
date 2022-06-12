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
python3 wikipedia-statistics/src/main/python/kafka_Producer.py --bootstrap-server localhost:29092 --topic-name wikipedia-events --events-to-produce 1000
```
You should see the producer being killed after 100 events.

## Queries

### Parameters
| Parameter   | Applies to   | Values                  |
| ----------- | ----------   | -----------             |
| time        | all requests | hour, week, month, year |
| filter      | all requests | all, per-lang, per-user-type |
| action      | all requests | countPagesCreated, countPagesModified, mostActiveUsers, mostActivePages |
| filterParam | mostActive requests | bot, user / English, Spanish, etc... |

### Simple Counts
`http://localhost:8000/wiki.stats/{time}/{filter}/{type}`
```
for example, quering counts of new pages created last month:
```http://localhost:8000/wiki.stats/month/all/countPagesCreated```

### Most Active Users
`
http://localhost:8000/wiki.stats/{time}/{filter}/{filterParam}/{type}
`
for example, quering top users last week which are not bots:
```http://localhost:8000/wiki.stats/week/all/user/mostActiveUsers```

### Most Active Pages
`
http://localhost:8000/wiki.stats/{time}/{filter}/{filterParam}/{type}
`
for example, quering top pages last week in English:
```http://localhost:8000/wiki.stats/week/all/English/mostActivePages```




