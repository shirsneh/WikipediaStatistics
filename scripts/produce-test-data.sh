docker-compose exec kafka bash -c "
  kafka-console-producer \
  --bootstrap-server kafka:9092 \
  --topic wikipedia-events \
  --property 'parse.key=true' \
  --property 'key.separator=|' < wikipedia-events.json"

# docker-compose exec kafka bash -c "
#   kafka-console-producer \
#   --bootstrap-server kafka:9092 \
#   --topic body-temp-events \
#   --property 'parse.key=true' \
#   --property 'key.separator=|' < body-temp-events.json"
