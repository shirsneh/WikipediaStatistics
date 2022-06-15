package cloud.wikipedia.model;

import java.io.Serializable;

public class WikiUser extends WikiObject implements Serializable {
  private boolean isBot;

  public WikiUser(WikiEvent wikiEvent) {
    super(wikiEvent.getUsername());
    this.isBot = wikiEvent.getUserType().equals("bot");
  }

  public WikiUser(WikiUser other) {
    super(other.getName());
    this.isBot = other.isBot;
    this.counter = other.counter;
  }

  public WikiUser(String name, boolean isBot) {
    super(name);
    this.isBot = isBot;
  }

  public static String getStreamName() {
    return "-mostActiveUsers";
  }

  public boolean getIsBot() {
    return isBot;
  }

  @Override
  public String toString() {
    return "WikiUser{" + "isBot=" + isBot + ", name='" + name + '\'' + ", counter=" + counter + '}';
  }
}
