package cloud.wikipedia.serialization.json;

import cloud.wikipedia.model.*;
import cloud.wikipedia.model.MostActive;
import cloud.wikipedia.model.WikiEvent;
import cloud.wikipedia.model.WikiObject;
import cloud.wikipedia.model.WikiUser;
import com.google.gson.reflect.TypeToken;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class JsonSerdes {
  public static Serde<WikiObject> WikiObject() {
    JsonSerializer<WikiObject> serializer = new JsonSerializer<>();
    JsonDeserializer<WikiObject> deserializer = new JsonDeserializer<>(WikiObject.class);
    return Serdes.serdeFrom(serializer, deserializer);
  }

  public static Serde<WikiEvent> WikiEvent() {
    JsonSerializer<WikiEvent> serializer = new JsonSerializer<>();
    JsonDeserializer<WikiEvent> deserializer = new JsonDeserializer<>(WikiEvent.class);
    return Serdes.serdeFrom(serializer, deserializer);
  }

  public static Serde<WikiUser> WikiUser() {
    JsonSerializer<WikiUser> serializer = new JsonSerializer<>();
    JsonDeserializer<WikiUser> deserializer = new JsonDeserializer<>(WikiUser.class);
    return Serdes.serdeFrom(serializer, deserializer);
  }

  public static Serde<MostActive<WikiObject>> MostActivePages() {
    JsonSerializer<MostActive<WikiObject>> serializer = new JsonSerializer<>();
    JsonDeserializer<MostActive<WikiObject>> deserializer =
        new JsonDeserializer<>(new TypeToken<MostActive<WikiObject>>() {}.getType());
    return Serdes.serdeFrom(serializer, deserializer);
  }

  public static Serde<MostActive<WikiUser>> MostActiveUsers() {
    JsonSerializer<MostActive<WikiUser>> serializer = new JsonSerializer<>();
    JsonDeserializer<MostActive<WikiUser>> deserializer =
        new JsonDeserializer<>(new TypeToken<MostActive<WikiUser>>() {}.getType());
    return Serdes.serdeFrom(serializer, deserializer);
  }
}
