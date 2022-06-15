package cloud.wikipedia.model;

import java.time.Duration;
import java.util.HashMap;

public class Utils {
  public static final int DAYS_IN_WEEK = 7;
  public static final int DAYS_IN_MONTH = 30;
  public static final int DAYS_IN_YEAR = 365;
  public static final int MOST_ACTIVE_LIMIT = 5;

  public static final HashMap<String, Duration> timeWindows =
      new HashMap<>() {
        {
          put("hour", Duration.ofHours(1));
          put("week", Duration.ofDays(Utils.DAYS_IN_WEEK));
          put("month", Duration.ofDays(Utils.DAYS_IN_MONTH));
          put("year", Duration.ofDays(Utils.DAYS_IN_YEAR));
        }
      };
}
