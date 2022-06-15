package cloud.wikipedia.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class MostActive<K extends WikiObject> {
  private final TreeSet<K> mostActive = new TreeSet<>();

  public MostActive<K> add(final K enriched) {
    mostActive.add(enriched);

    // keep only the top 5 high scores
    if (mostActive.size() > Utils.MOST_ACTIVE_LIMIT) {
      mostActive.remove(mostActive.last());
    }

    return this;
  }

  public List<K> toList() {
    Iterator<K> objects = mostActive.iterator();
    List<K> objectScores = new ArrayList<>();

    while (objects.hasNext()) {
      objectScores.add(objects.next());
    }

    return objectScores;
  }
}
