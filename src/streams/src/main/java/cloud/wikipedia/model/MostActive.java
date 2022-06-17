package cloud.wikipedia.model;

import java.util.*;
import java.util.stream.Collectors;

public class MostActive<K extends WikiObject> {
  private final HashMap<String, K> mostActive = new HashMap<>();

  public MostActive<K> add(final K enriched) {
    if (mostActive.containsKey(enriched.name)) {
      enriched.setCounter(enriched.counter + mostActive.get(enriched.name).counter);
    }
    mostActive.put(enriched.name, enriched);
    return this;
  }

  public List<K> toList() {
    List<K> most = new ArrayList<>(mostActive.values());
    most.sort((k, t1) -> Integer.compare(t1.counter, k.counter));

    List<K> topN = most.stream().limit(Utils.MOST_ACTIVE_LIMIT).collect(Collectors.toList());

    //
    //
    //        Iterator<K> objects = mostActive.iterator();
    //        List<K> objectScores = new ArrayList<>();
    //
    //        while (objects.hasNext()) {
    //            objectScores.add(objects.next());
    //        }

    return topN;
  }
}
