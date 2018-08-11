package de.gurkenlabs.ldjam42;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.ldjam42.entities.PartyGuest;
import de.gurkenlabs.ldjam42.entities.PartyGuestSpawner;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.MapArea;
import de.gurkenlabs.litiengine.entities.Spawnpoint;
import de.gurkenlabs.litiengine.environment.EntitySpawner;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.EnvironmentAdapter;
import de.gurkenlabs.litiengine.environment.EnvironmentEntityAdapter;
import de.gurkenlabs.litiengine.environment.IEnvironment;

public final class GameManager {
  // 09:00 pm
  public static long START_TIME = 72000000;

  // 06:00 am the next day
  public static long END_TIME = 104400000;

  private static IEnvironment goin;
  private static long startedTicks;
  private static EntitySpawner<PartyGuest> spawner;

  private static EnumMap<ClubArea, Collection<MapArea>> areas = new EnumMap<>(ClubArea.class);

  private static List<PartyGuest> kickedPartyGuests = new CopyOnWriteArrayList<>();

  private GameManager() {
  }

  public static void init() {
    goin = new Environment(Game.getMap("test"));
    goin.addListener(new EnvironmentAdapter() {
      @Override
      public void environmentLoaded(IEnvironment environment) {
        spawner = new PartyGuestSpawner(getSpawnPoints(), 1000, 5);
        Game.getLoop().execute(10500, () -> {
          spawner.setInterval(15000);
        });

        areas.put(ClubArea.CHILLAREA, environment.getByTag(MapArea.class, "area_chill"));
        if (areas.get(ClubArea.CHILLAREA).isEmpty()) {
          throw new IllegalArgumentException("No area_chill.");
        }

        areas.put(ClubArea.DANCEFLOOR, environment.getByTag(MapArea.class, "area_dance"));
        if (areas.get(ClubArea.DANCEFLOOR).isEmpty()) {
          throw new IllegalArgumentException("No area_dance.");
        }

        areas.put(ClubArea.PIZZASTAND, environment.getByTag(MapArea.class, "area_food"));
        if (areas.get(ClubArea.PIZZASTAND).isEmpty()) {
          throw new IllegalArgumentException("No area_food.");
        }

        areas.put(ClubArea.BAR, environment.getByTag(MapArea.class, "area_drink"));
        if (areas.get(ClubArea.BAR).isEmpty()) {
          throw new IllegalArgumentException("No area_drink.");
        }

        areas.put(ClubArea.LOBBY, environment.getByTag(MapArea.class, "area_entry"));
        if (areas.get(ClubArea.LOBBY).isEmpty()) {
          throw new IllegalArgumentException("No area_entry.");
        }
      }
    });

    goin.addEntityListener(new EnvironmentEntityAdapter() {
      @Override
      public void entityRemoved(IEntity entity) {
        // remember kicked party guests
        if (entity instanceof PartyGuest) {
          kickedPartyGuests.add((PartyGuest) entity);
        }
      }
    });
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

  public static int getGuests(ClubArea area) {
    return getGuestsInAreas(area);
  }

  public static int getTotalGuestsInMainAreas() {
    return (int) Game.getEnvironment().getByType(PartyGuest.class).stream().filter(x -> areas.get(ClubArea.LOBBY).stream().noneMatch(a -> a.getBoundingBox().intersects(x.getCollisionBox()))).count();
  }

  private static int getGuestsInAreas(ClubArea area) {
    return areas.get(area).stream().mapToInt(GameManager::countGuestsInArea).sum();
  }

  private static int countGuestsInArea(MapArea area) {
    int count = 0;
    for (PartyGuest guest : Game.getEnvironment().getByType(PartyGuest.class)) {
      if (guest.getCollisionBox().intersects(area.getBoundingBox())) {
        count++;
      }
    }

    return count;
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
}
