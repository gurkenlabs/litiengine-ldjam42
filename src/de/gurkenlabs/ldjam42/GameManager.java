package de.gurkenlabs.ldjam42;

import java.sql.Time;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.IEnvironment;

public final class GameManager {
  // 09:00 pm
  public static long START_TIME = 72000000;

  // 06:00 am the next day
  public static long END_TIME = 104400000;

  private static IEnvironment goin;
  private static long startedTicks;

  private GameManager() {
  }

  public static void init() {
    goin = new Environment(Game.getMap("test"));
  }

  public static void start() {
    startedTicks = Game.getLoop().getTicks();
  }

  public static IEnvironment getGoin() {
    return goin;
  }

  public static Time getStartTime() {
    return new Time(START_TIME);
  }

  public static Time getEndTime() {
    return new Time(END_TIME);
  }

  public static Time getCurrentGameTime() {
    // 1 minute of playtime is 1 second in real time
    return new Time(START_TIME + Game.getLoop().getDeltaTime(startedTicks) * 60);
  }
}
