package cloud.wikipedia.model;

import java.io.Serializable;
import org.jetbrains.annotations.NotNull;

public class WikiObject implements Comparable<WikiObject>, Serializable {
  protected String name;
  protected int counter;

  public WikiObject(String name) {
    this.name = name;
    this.counter = 1;
  }

  public WikiObject(WikiEvent event) {
    this.name = event.getTitle();
    this.counter = 1;
  }

  public static String getStreamName() {
    return "-mostActivePages";
  }

  public int getCounter() {
    return counter;
  }

  public void setCounter(int counter) {
    this.counter = counter;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "WikiActive{" + "name='" + name + '\'' + ", counter=" + counter + '}';
  }

  @Override
  public int compareTo(@NotNull WikiObject wikiActive) {
    return this.name.compareTo(wikiActive.name);
  }
}
