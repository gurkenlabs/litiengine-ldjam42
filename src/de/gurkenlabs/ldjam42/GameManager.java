package de.gurkenlabs.ldjam42;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.ldjam42.entities.PartyGuest;
import de.gurkenlabs.ldjam42.entities.PartyGuestSpawner;
import de.gurkenlabs.ldjam42.util.IntPermutator;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.MapArea;
import de.gurkenlabs.litiengine.entities.Spawnpoint;
import de.gurkenlabs.litiengine.environment.EntitySpawner;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.EnvironmentAdapter;
import de.gurkenlabs.litiengine.environment.EnvironmentEntityAdapter;
import de.gurkenlabs.litiengine.environment.IEnvironment;
import de.gurkenlabs.litiengine.pathfinding.astar.AStarGrid;

public final class GameManager {
  // 09:00 pm
  public static long START_TIME = 72000000;

  // 06:00 am the next day
  public static long END_TIME = 104400000;

  private static IntPermutator featurePermutations;

  private static IEnvironment goin;
  private static long startedTicks;
  private static EntitySpawner<PartyGuest> spawner;

  private static EnumMap<ClubArea, Collection<MapArea>> areas = new EnumMap<>(ClubArea.class);
  private static EnumMap<ClubArea, Double> space = new EnumMap<>(ClubArea.class);
  private static EnumMap<ClubArea, Double> remaining = new EnumMap<>(ClubArea.class);
  private static EnumMap<ClubArea, Integer> guestsInArea = new EnumMap<>(ClubArea.class);
  private static int totalGuestsInMainAreas;

  private static AStarGrid grid;

  private static List<PartyGuest> kickedPartyGuests = new CopyOnWriteArrayList<>();
  private static volatile int currentMoney;

  private GameManager() {
  }

  public static void init() {
    featurePermutations = new IntPermutator(4);
    goin = new Environment(Game.getMap("test"));
    goin.addListener(new EnvironmentAdapter() {
      @Override
      public void environmentLoaded(IEnvironment environment) {
        spawner = new PartyGuestSpawner(getSpawnPoints(), 1000, 5);
        Game.getLoop().execute(10500, () -> {
          spawner.setInterval(15000);
        });

        for (ClubArea area : ClubArea.values()) {
          areas.put(area, environment.getByTag(MapArea.class, area.getTag()));
          if (areas.get(area).isEmpty()) {
            throw new IllegalArgumentException("No " + area.getTag());
          }
        }

        // calculate surfaces
        for (Map.Entry<ClubArea, Collection<MapArea>> entry : areas.entrySet()) {
          ClubArea ar = entry.getKey();

          double s = 0;
          for (MapArea mapArea : entry.getValue()) {
            s += mapArea.getBoundingBox().getWidth() * mapArea.getBoundingBox().getHeight();
          }

          space.put(ar, s);
        }

        grid = new AStarGrid(Game.getEnvironment().getMap().getSizeInPixels(), 8);
        Game.getLoop().attach(GameManager::update);
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

  public static void update() {
    for (ClubArea area : ClubArea.values()) {
      final int GUEST_SPACE = 16 * 16;
      double totalSpace = space.get(area);

      double guestsCount = getGuestsInAreas(area);
      double guestSpace = guestsCount * GUEST_SPACE;

      remaining.put(area, Math.max((totalSpace - guestSpace) / totalSpace, 0));
      guestsInArea.put(area, getGuestsInAreas(area));
    }

    totalGuestsInMainAreas = (int) Game.getEnvironment().getByType(PartyGuest.class).stream().filter(x -> areas.get(ClubArea.LOBBY).stream().noneMatch(a -> a.getBoundingBox().intersects(x.getCollisionBox()))).count();
  }

  public static void start() {
    startedTicks = Game.getLoop().getTicks();
  }

  public static AStarGrid getGrid() {
    return grid;
  }

  public static int getCurrentMoney() {
    return currentMoney;
  }

  public static IEnvironment getGoin() {
    return goin;
  }

  public static List<int[]> getFeaturePermutations() {
    return featurePermutations.getPermutations();
  }

  public static Time getStartTime() {
    return new Time(START_TIME);
  }

  public static void spendMoney(int money) {
    currentMoney += money;
  }

  public static Time getEndTime() {
    return new Time(END_TIME);
  }

  public static Time getCurrentGameTime() {
    // 1 minute of playtime is 1 second in real time
    return new Time(START_TIME + Game.getLoop().getDeltaTime(startedTicks) * 60);
  }

  public static int getGuests(ClubArea area) {
    if (guestsInArea == null || guestsInArea.get(area) == null) {
      return 0;
    }
    return guestsInArea.get(area);
  }

  public static double getGuestsRelative(ClubArea area) {
    double total = getTotalGuestsInMainAreas();
    double count = guestsInArea.get(area);
    if (total == 0) {
      return 0;
    }

    return count / total;
  }

  public static double getRemainingSpace(ClubArea area) {
    return remaining.get(area);
  }

  public static int getTotalGuestsInMainAreas() {
    return totalGuestsInMainAreas;
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
