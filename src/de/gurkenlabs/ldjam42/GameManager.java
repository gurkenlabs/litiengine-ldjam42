package de.gurkenlabs.ldjam42;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import de.gurkenlabs.ldjam42.entities.PartyGuest;
import de.gurkenlabs.ldjam42.entities.PartyGuestSpawner;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Spawnpoint;
import de.gurkenlabs.litiengine.environment.EntitySpawner;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.EnvironmentAdapter;
import de.gurkenlabs.litiengine.environment.IEnvironment;

public final class GameManager {
  // 09:00 pm
  public static long START_TIME = 72000000;

  // 06:00 am the next day
  public static long END_TIME = 104400000;

  private static IEnvironment goin;
  private static long startedTicks;
  private static EntitySpawner<PartyGuest> spawner;

  private GameManager() {
  }

  public static void init() {
    goin = new Environment(Game.getMap("test"));
    goin.addListener(new EnvironmentAdapter() {
      @Override
      public void environmentLoaded(IEnvironment environment) {
        spawner = new PartyGuestSpawner(getSpawnPoints(), 15000, 5);
      }
    });
  }

  private static List<Spawnpoint> getSpawnPoints() {
    ArrayList<Spawnpoint> points = new ArrayList<>();
    Spawnpoint point = Game.getEnvironment().getSpawnpoint("party-spawn");
    if (point == null) {
      throw new IllegalArgumentException("No party guest spawnpoint found on the map.");
    }
    points.add(point);
    return points;
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
