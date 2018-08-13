package de.gurkenlabs.ldjam42.entities;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import de.gurkenlabs.ldjam42.ClubArea;
import de.gurkenlabs.ldjam42.GameManager;
import de.gurkenlabs.ldjam42.TileUtilities;
import de.gurkenlabs.ldjam42.graphics.CoinEmitter;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.MapArea;
import de.gurkenlabs.litiengine.environment.tilemap.MapUtilities;
import de.gurkenlabs.litiengine.pathfinding.EntityNavigator;
import de.gurkenlabs.litiengine.pathfinding.IEntityNavigator;
import de.gurkenlabs.litiengine.pathfinding.astar.AStarPathFinder;
import de.gurkenlabs.litiengine.physics.MovementController;
import de.gurkenlabs.litiengine.util.ArrayUtilities;
import de.gurkenlabs.litiengine.util.MathUtilities;

public class PartyGuestController extends MovementController<PartyGuest> {
  public static Map<PartyGuest, Point2D> currentTargets = new ConcurrentHashMap<>();

  // STATE MONEY
  private static final int PAY_MIN_SPEND = 5;
  private static final int PAY_MIN_INTERVAL = 6000;
  private static final int PAY_MAX_INTERVAL = 11000;
  private int paymentInterval;
  private long lastPayment;

  // STATE CHANGE_AREA
  private static final int CHANGE_AREA_MIN = 10000;
  private static final int CHANGE_AREA_MAX = 60000;
  private long lastChangeArea;
  private long nextChangeInterval;

  private EntityNavigator nav;

  public PartyGuestController(PartyGuest mobileEntity) {
    super(mobileEntity);
  }

  @Override
  public void update() {
    super.update();

    this.updateState();

    switch (this.getEntity().getState()) {
    case PARTY:
      this.party();
      break;
    case BAD_BEHAVIOR:
      break;
    case CHANGE_AREA:
      this.changeArea();
      break;
    case SPEND_MONEY:
      this.spendMoney();
      break;
    default:
      break;
    }
  }

  public static boolean isTargeted(Point2D target) {
    for (Point2D point : currentTargets.values()) {
      if (MathUtilities.equals(target.getX(), point.getX(), 1) && MathUtilities.equals(target.getY(), point.getY(), 1)) {
        return true;
      }
    }

    return false;
  }

  public static void remove(PartyGuest guest) {
    currentTargets.remove(guest);
  }

  public IEntityNavigator getNavigator() {
    return this.nav;
  }

  private void changeArea() {
    if (this.nav != null && this.nav.isNavigating()) {
      // still navigating
      return;
    }

    this.initEntityNavigator();
    if (this.nav == null) {
      return;
    }

    ClubArea[] areas = ClubArea.values();
    areas = ArrayUtilities.remove(areas, ClubArea.LOBBY);
    ClubArea targetArea = ArrayUtilities.getRandom(areas);
    List<MapArea> mapAreas = GameManager.getMapAreas(targetArea).stream().collect(Collectors.toList());
    double[] probabilities = new double[mapAreas.size()];

    // select a maparea with a probability relative to their size
    double total = GameManager.getTotalSpace(targetArea);
    for (int i = 0; i < probabilities.length; i++) {
      double space = mapAreas.get(i).getBoundingBox().getWidth() * mapAreas.get(i).getBoundingBox().getHeight();
      double probability = space / total;
      probabilities[i] = probability;
    }

    int index = MathUtilities.getRandomIndex(probabilities);

    MapArea targetMapArea = mapAreas.get(index);
    Point2D target = TileUtilities.getRandomTileCenterLocation(targetMapArea.getBoundingBox());
    this.nav.navigate(target);
    currentTargets.put(this.getEntity(), target);
  }

  private void initEntityNavigator() {
    if (this.nav == null && Game.getEnvironment() != null && Game.getEnvironment().isLoaded()) {
      this.nav = new EntityNavigator(this.getEntity(), new AStarPathFinder(GameManager.getGrid()));
      this.nav.setAcceptableError(1);
      this.nav.addNavigationListener(() -> {
        TileUtilities.centerEntityInCurrentTile(this.getEntity());

        // update target after snapping
        Rectangle2D tileBounds = MapUtilities.getTileBoundingBox(MapUtilities.getTile(this.getEntity().getCollisionBoxCenter()));
        Point2D target = new Point2D.Double(tileBounds.getCenterX(), tileBounds.getCenterY());
        currentTargets.put(this.getEntity(), target);

        this.lastChangeArea = Game.getLoop().getTicks();
        this.nextChangeInterval = MathUtilities.randomInRange(CHANGE_AREA_MIN, CHANGE_AREA_MAX);
      });
    }
  }

  private void party() {
  }

  private void updateState() {

    this.getEntity().updateSatisfaction();

    if (Game.getLoop().getDeltaTime(this.lastPayment) > paymentInterval) {
      this.getEntity().setState(State.SPEND_MONEY);
      return;
    }

    if (Game.getLoop().getDeltaTime(this.lastChangeArea) > this.nextChangeInterval) {

    }

    if (!this.getEntity().getCurrentArea().isMainArea()
        || Game.getLoop().getDeltaTime(this.lastChangeArea) > this.nextChangeInterval
        || this.nav != null && this.nav.isNavigating() || this.hasOtherTarget()) {
      this.getEntity().setState(State.CHANGE_AREA);
      return;
    }

    this.getEntity().setState(State.PARTY);
  }

  private boolean hasOtherTarget() {
    if (!currentTargets.containsKey(this.getEntity())) {
      return false;
    }

    final Point2D current = currentTargets.get(this.getEntity());
    for (PartyGuest guest : currentTargets.keySet()) {
      if (guest.equals(this.getEntity())) {
        continue;
      }

      if (MathUtilities.equals(currentTargets.get(guest).getX(), current.getX(), 1) && MathUtilities.equals(currentTargets.get(guest).getY(), current.getY(), 1)) {
        return true;
      }
    }

    return false;
  }

  private void spendMoney() {
    // only spend money in main area and if we have any satisfaction
    if (this.getEntity().getSatisfaction() == 0 || !this.getEntity().getCurrentArea().isMainArea()) {
      this.paymentInterval = MathUtilities.randomInRange(PAY_MIN_INTERVAL, PAY_MAX_INTERVAL);
      this.lastPayment = Game.getLoop().getTicks();
      return;
    }

    int money = (int) (Math.round(PAY_MIN_SPEND + (float) Math.pow(this.getEntity().getWealth(), 2)) * this.getEntity().getSatisfaction());
    GameManager.spendMoney(money);
    Game.getEnvironment().add(new CoinEmitter(this.getEntity().getCenter()));
    this.paymentInterval = MathUtilities.randomInRange(PAY_MIN_INTERVAL, PAY_MAX_INTERVAL);
    this.lastPayment = Game.getLoop().getTicks();
  }
}
