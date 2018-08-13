package de.gurkenlabs.ldjam42;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.SwingUtilities;

import de.gurkenlabs.ldjam42.entities.Feature;
import de.gurkenlabs.ldjam42.entities.Gender;
import de.gurkenlabs.ldjam42.entities.PartyGuest;
import de.gurkenlabs.ldjam42.entities.PartyGuestController;
import de.gurkenlabs.ldjam42.entities.PartyGuestSpawner;
import de.gurkenlabs.ldjam42.graphics.DismissEmitter;
import de.gurkenlabs.ldjam42.gui.IngameScreen;
import de.gurkenlabs.ldjam42.util.IntCombinator;
import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.MapArea;
import de.gurkenlabs.litiengine.entities.Spawnpoint;
import de.gurkenlabs.litiengine.environment.EntitySpawner;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.EnvironmentAdapter;
import de.gurkenlabs.litiengine.environment.EnvironmentEntityAdapter;
import de.gurkenlabs.litiengine.environment.IEnvironment;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.pathfinding.astar.AStarGrid;
import de.gurkenlabs.litiengine.util.ArrayUtilities;
import de.gurkenlabs.litiengine.util.ImageProcessing;

public final class GameManager {
  // 09:00 pm
  public static long START_TIME = 72000000;

  // 06:00 am the next day
  public static long END_TIME = 104400000;

  private static IntCombinator featurePermutations;

  private static IEnvironment goin;
  private static long startedTicks;
  private static long lastFlash;
  private static int flashInterval = 30000;
  private static EntitySpawner<PartyGuest> spawner;

  private static EnumMap<ClubArea, Collection<MapArea>> areas = new EnumMap<>(ClubArea.class);
  private static EnumMap<ClubArea, Double> space = new EnumMap<>(ClubArea.class);
  private static EnumMap<ClubArea, Double> remaining = new EnumMap<>(ClubArea.class);
  private static EnumMap<ClubArea, Integer> guestsInArea = new EnumMap<>(ClubArea.class);
  private static List<PartyGuest> kickedPartyGuests = new CopyOnWriteArrayList<>();
  private static int totalGuestsInMainAreas;

  private static AStarGrid grid;
  private static volatile int currentMoney;

  private static PartyGuest currentFocus;

  private static GameState gameState;

  private static boolean isRestarting;

  private GameManager() {
  }

  public static void init() {
    featurePermutations = new IntCombinator(4);
    lastFlash = 0;
    generateGuestSpritesheets();

    Input.mouse().onPressed(e -> {
      Game.getScreenManager().getRenderComponent().setCursor(Program.CURSOR_CLICK);
      Game.getScreenManager().getRenderComponent().setCursorOffset(0, 0);

      if (e.isConsumed()) {
        return;
      }

      if (SwingUtilities.isLeftMouseButton(e) && getGameState() == GameState.INGAME) {
        setCurrentFocus(getFocusedGuest());
      }
    });

    Input.mouse().onReleased(p -> {
      Game.getScreenManager().getRenderComponent().setCursor(Program.CURSOR_STANDARD);
      Game.getScreenManager().getRenderComponent().setCursorOffset(0, 0);
    });

    // debug option to test the end screen
    Input.keyboard().onKeyTyped(KeyEvent.VK_F5, e -> {
      if (Game.getConfiguration().debug().isDebugEnabled()) {
        setGameState(GameState.ENDSCREEN);
      }
    });

    goin = new Environment(Game.getMap("club1"));
    addListeners(goin);
  }

  private static void addListeners(IEnvironment env) {
    env.addListener(new EnvironmentAdapter() {
      @Override
      public void environmentLoaded(IEnvironment environment) {
        spawner = new PartyGuestSpawner(getSpawnPoints(), 1000, 5);
        Game.getLoop().execute(10500, () -> {
          spawner.setInterval(11000);
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

        grid = new AStarGrid(Game.getEnvironment().getMap().getSizeInPixels(), 16);
        Game.getLoop().attach(GameManager::update);

        IngameScreen.instance().getHud().hideDismissButton();
      }
    });

    env.addEntityListener(new EnvironmentEntityAdapter() {
      @Override
      public void entityRemoved(IEntity entity) {
        // remember kicked party guests
        if (entity instanceof PartyGuest) {
          kickedPartyGuests.add((PartyGuest) entity);
          PartyGuestController.remove((PartyGuest) entity);
        }
      }
    });
  }

  public static void restart() {
    isRestarting = true;
    try {
      goin = new Environment(Game.getMap("club1"));
      addListeners(goin);
      space.clear();
      remaining.clear();
      guestsInArea.clear();
      kickedPartyGuests.clear();
      areas.clear();
      totalGuestsInMainAreas = 0;
      PartyGuestController.currentTargets.clear();

      currentFocus = null;
      currentMoney = 0;
      Game.loadEnvironment(GameManager.getGoin());
      startedTicks = Game.getLoop().getTicks();
    } finally {
      isRestarting = false;
    }
  }

  public static void dismiss() {
    if (currentFocus == null) {
      return;
    }

    Game.getEnvironment().add(new DismissEmitter(currentFocus));
    Game.getEnvironment().remove(currentFocus);
    kickedPartyGuests.add(currentFocus);
    setCurrentFocus(null);
  }

  public static List<PartyGuest> getKickedGuests() {
    return kickedPartyGuests;
  }

  public static void update() {
    if (isRestarting) {
      return;
    }

    for (ClubArea area : ClubArea.values()) {
      double totalSpace = space.get(area);

      double guestsCount = getGuestsInAreas(area);
      double guestSpace = guestsCount * Math.pow(PartyGuest.OCCUPATION, 2);

      remaining.put(area, Math.max((totalSpace - guestSpace) / totalSpace, 0));
      guestsInArea.put(area, getGuestsInAreas(area));
    }

    totalGuestsInMainAreas = (int) Game.getEnvironment().getByType(PartyGuest.class).stream().filter(x -> areas.get(ClubArea.LOBBY).stream().noneMatch(a -> a.getBoundingBox().intersects(x.getCollisionBox()))).count();

    if (getGameState() != GameState.ENDSCREEN && getCurrentGameTime().getTime() >= getEndTime().getTime()) {
      setGameState(GameState.ENDSCREEN);
    }

//let someone flash after a certain time period
    if (Game.getLoop().convertToMs(Game.getLoop().getTicks()) - lastFlash > flashInterval) {
      PartyGuest flasher = (PartyGuest) ArrayUtilities.getRandom(Game.getEnvironment().getByType(PartyGuest.class).toArray());
      if (!flasher.isFlashing()) {
        flasher.flash();
        lastFlash = Game.getLoop().convertToMs(Game.getLoop().getTicks());
      }
    }
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
    if (getGameState() == GameState.ENDSCREEN) {
      return getEndTime();
    }

    // 1 minute of playtime is 1 second in real time
    return new Time(START_TIME + Game.getLoop().getDeltaTime(startedTicks) * 60);
  }

  public static int getGuests(ClubArea area) {
    return (guestsInArea != null && guestsInArea.get(area) != null) ? guestsInArea.get(area) : 0;
  }

  public static double getGuestsRelative(ClubArea area) {
    double total = getTotalGuestsInMainAreas();
    double count = guestsInArea.get(area);
    if (total == 0) {
      return 0;
    }

    return count / total;
  }

  public static double getTotalSpace(ClubArea area) {
    if (space.containsKey(area)) {
      return space.get(area);
    }

    return 0;
  }

  public static double getRemainingSpace(ClubArea area) {
    if (remaining.containsKey(area)) {
      return remaining.get(area);
    }

    return 0;
  }

  public static Collection<MapArea> getMapAreas(ClubArea area) {
    if (areas.containsKey(area)) {
      return areas.get(area);
    }
    return new ArrayList<>();
  }

  public static int getTotalGuestsInMainAreas() {
    return totalGuestsInMainAreas;
  }

  public static PartyGuest getCurrentFocus() {
    return currentFocus;
  }

  public static void setCurrentFocus(PartyGuest focus) {
    currentFocus = focus;
    if (currentFocus == null) {
      IngameScreen.instance().getHud().hideDismissButton();
    } else {
      IngameScreen.instance().getHud().showDismissButton();
    }
  }

  private static PartyGuest getFocusedGuest() {
    if (Game.getEnvironment() == null) {
      return null;
    }

    Collection<PartyGuest> guests = Game.getEnvironment().getByType(PartyGuest.class);
    Optional<PartyGuest> guest = guests.stream().filter(x -> x.getHitBox().contains(Input.mouse().getMapLocation())).findFirst();
    if (guest.isPresent()) {
      return guest.get();
    }

    return null;
  }

  private static int getGuestsInAreas(ClubArea area) {
    if (areas.containsKey(area)) {
      return areas.get(area).stream().mapToInt(GameManager::countGuestsInArea).sum();
    }
    return 0;
  }

  private static void generateGuestSpritesheets() {
    String[] states = { "walk", "idle" };
    int spriteWidth = 20;
    int spriteHeight = 22;
    int foundSpritesheets = 0;
    for (Gender gender : Gender.values()) {
      for (int[] permutation : getFeaturePermutations()) {
        for (String state : states) {
          for (Direction direction : Direction.values()) {
            String gend = gender.toString().toLowerCase();
            String dir = direction.toString().toLowerCase();
            String baseSpritePath = String.format("%s-%s-%s", gend, state, dir);
            Spritesheet baseSpritesheet = Spritesheet.find(baseSpritePath);
            if (baseSpritesheet == null) {
              continue;
            }

            BufferedImage baseSprite = baseSpritesheet.getImage();
            BufferedImage combinedSprite = ImageProcessing.getCompatibleImage(baseSprite.getWidth(), baseSprite.getHeight());
            Graphics2D g = combinedSprite.createGraphics();
            g.drawImage(baseSprite, 0, 0, baseSprite.getWidth(), baseSprite.getHeight(), null);
            for (Feature feature : Feature.values()) {
              String feat = feature.toString().toLowerCase();
              int featIndex = feature.ordinal();
              String featureSpritePath = String.format("%s-%s%d-%s-%s", gend, feat, permutation[featIndex], state, dir);
              BufferedImage featureSprite = Spritesheet.find(featureSpritePath).getImage();
              g.drawImage(featureSprite, 0, 0, featureSprite.getWidth(), featureSprite.getHeight(), null);
            }
            g.dispose();
            String combinedSpritePath = String.format("%s-%d_%d_%d_%d-%s-%s", gend, permutation[0], permutation[1], permutation[2], permutation[3], state, dir);

            Spritesheet.load(combinedSprite, combinedSpritePath, spriteWidth, spriteHeight);
            foundSpritesheets++;
          }
        }

      }
    }
    System.out.println("total number of spritesheets: " + foundSpritesheets);
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
    Collection<Spawnpoint> points = Game.getEnvironment().getByTag(Spawnpoint.class, "entry");
    if (points.isEmpty()) {
      throw new IllegalArgumentException("No party guest spawnpoint found on the map.");
    }
    return new ArrayList<>(points);
  }

  public static GameState getGameState() {
    return gameState;
  }

  public static void setGameState(GameState gameState) {
    GameManager.gameState = gameState;
  }
}
