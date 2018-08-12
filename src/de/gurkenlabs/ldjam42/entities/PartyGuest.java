package de.gurkenlabs.ldjam42.entities;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import de.gurkenlabs.ldjam42.BadBehavior;
import de.gurkenlabs.ldjam42.ClubArea;
import de.gurkenlabs.ldjam42.GameManager;
import de.gurkenlabs.ldjam42.Needs;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.annotation.CollisionInfo;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.annotation.MovementInfo;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.entities.MapArea;
import de.gurkenlabs.litiengine.graphics.DebugRenderer;
import de.gurkenlabs.litiengine.graphics.TextRenderer;
import de.gurkenlabs.litiengine.graphics.animation.EntityAnimationController;
import de.gurkenlabs.litiengine.physics.MovementController;
import de.gurkenlabs.litiengine.util.ArrayUtilities;
import de.gurkenlabs.litiengine.util.MathUtilities;

@MovementInfo(velocity = 50)
@EntityInfo(width = 11, height = 22)
@CollisionInfo(collision = true, collisionBoxWidth = 11, collisionBoxHeight = 11)
public class PartyGuest extends Creature {
  private static final int HIGHEST_NEED_VALUE = 3;
  private static final int AVG_NEED_VALUE = 2;
  private static final int LOW_NEED_VALUE = 1;
  private static final int MAX_GROUP_SIZE = 9;
  private static final int ENDURANCE = 180000;

  private static int currentGroupId;
  private static double currentGroupProbability = 1;
  private static int currentGroupSize;

  private int group;
  private final EnumMap<Needs, Integer> needs;
  private BadBehavior badBehavior;
  private final Gender gender;
  private State state;
  private int wealth;
  private double satisfaction;
  private long clubEntered;
  private ClubArea currentArea;

  static {
    DebugRenderer.addEntityDebugListener((g, e) -> {
      if (e instanceof PartyGuest) {
        PartyGuest guest = (PartyGuest) e;
        g.setColor(Color.BLACK);
        g.setFont(g.getFont().deriveFont(Font.BOLD, 4f));
        final int x = (int) Game.getCamera().getViewPortDimensionCenter(e).getX();
        final int y = (int) Game.getCamera().getViewPortDimensionCenter(e).getY() - 10;
        TextRenderer.render(g, Double.toString(guest.getSatisfaction()), x, y);

        if (guest.getHighestNeed() != null) {
          TextRenderer.render(g, guest.getHighestNeed().toString(), x, y + 5);
        }
        if (guest.getCurrentArea() != null) {
          TextRenderer.render(g, guest.getCurrentArea().toString(), x, y + 10);
        }
      }
    });
  }

  public PartyGuest(Point2D location) {
    this.needs = new EnumMap<>(Needs.class);
    this.gender = MathUtilities.randomBoolean() ? Gender.FEMALE : Gender.MALE;
    this.satisfaction = 1;

    this.setLocation(location);
    // TODO: evaluate random apperance
    this.setController(EntityAnimationController.class, new PartyGuestAnimationController(this));
    this.setVelocity((short) MathUtilities.randomInRange(20, 50));
    this.setController(MovementController.class, new PartyGuestController(this));
    this.initialize();
  }

  @Override
  public boolean canCollideWith(final ICollisionEntity otherEntity) {
    if (otherEntity instanceof PartyGuest) {
      return false;
    }

    return true;
  }

  @Override
  public void loaded() {
    super.loaded();
    this.clubEntered = Game.getLoop().getTicks();
  }

  public int getGroup() {
    return this.group;
  }

  public ClubArea getCurrentArea() {
    return this.currentArea;
  }

  public Map<Needs, Integer> getNeeds() {
    return this.needs;
  }

  public Needs getHighestNeed() {
    for (Needs need : this.needs.keySet()) {
      if (this.needs.get(need) == HIGHEST_NEED_VALUE) {
        return need;
      }
    }

    return null;
  }

  public BadBehavior getBadBehavior() {
    return this.badBehavior;
  }

  public double getSatisfaction() {
    return this.satisfaction;
  }

  public Gender getGender() {
    return this.gender;
  }

  public int getWealth() {
    return this.wealth;
  }

  public void setBadBehavior(BadBehavior behavior) {
    this.badBehavior = behavior;
  }

  public State getState() {
    return this.state;
  }

  public void setState(State state) {
    this.state = state;
  }

  public void updateSatisfaction() {
    this.updateCurrentArea();
    final double CURRENT_AREA_WEIGHT = 2.0;
    final double CAPACITY_WEIGHT = 1.0;
    final double ENDURANCE_WEIGHT = 0.5;

    // update by endurance
    double endurance = Math.max(1 - (Game.getLoop().getDeltaTime(this.clubEntered)) / (double) ENDURANCE, 0);
    if (endurance == 0) {
      this.satisfaction = 0;
      return;
    }

    // update by the amount of guests in all areas
    double capacity = 0;
    for (Needs need : this.needs.keySet()) {
      ClubArea area = ClubArea.getArea(need);
      if (area == ClubArea.LOBBY) {
        continue;
      }

      double rel = (1 - GameManager.getGuestsRelative(ClubArea.getArea(need))) * this.needs.get(need);
      capacity += rel;
    }

    capacity /= HIGHEST_NEED_VALUE + AVG_NEED_VALUE * 2 + LOW_NEED_VALUE;

    // update by value for the are the guest is currently in
    ClubArea current = this.getCurrentArea();
    double need = current.getNeed() == null ? 0 : this.needs.get(current.getNeed()) / (double) HIGHEST_NEED_VALUE;

    double capWeight = CAPACITY_WEIGHT * endurance;
    this.satisfaction = (capWeight * capacity + ENDURANCE_WEIGHT * endurance + need * CURRENT_AREA_WEIGHT) / (capWeight + ENDURANCE_WEIGHT + CURRENT_AREA_WEIGHT);
  }

  private void updateCurrentArea() {
    for (MapArea area : Game.getEnvironment().getAreas()) {
      if (!area.getBoundingBox().intersects(this.getCollisionBox())) {
        continue;
      }
      Optional<String> tag = area.getTags().stream().filter(x -> x.startsWith("area")).findFirst();
      if (tag.isPresent()) {
        this.currentArea = ClubArea.getAreaByTag(tag.get());
        break;
      }
    }
  }

  private void initialize() {

    // init group
    this.group = getGroupId();

    this.initializeNeeds();

    this.initializeWealth();
  }

  private void initializeWealth() {
    final int DEFAULT_WEALTH_MIN = 1;
    final int DEFAULT_WEALTH_MAX = 5;
    this.wealth = MathUtilities.randomInRange(DEFAULT_WEALTH_MIN, DEFAULT_WEALTH_MAX);
    // TODO: implement VIP
  }

  private void initializeNeeds() {
    Needs[] n = Needs.values();
    Needs highestNeed = ArrayUtilities.getRandom(n);
    this.needs.put(highestNeed, HIGHEST_NEED_VALUE);
    n = ArrayUtilities.remove(n, highestNeed);

    Needs avgNeed1 = ArrayUtilities.getRandom(n);
    this.needs.put(avgNeed1, AVG_NEED_VALUE);
    n = ArrayUtilities.remove(n, avgNeed1);

    Needs avgNeed2 = ArrayUtilities.getRandom(n);
    this.needs.put(avgNeed2, AVG_NEED_VALUE);
    n = ArrayUtilities.remove(n, avgNeed2);

    Needs lowNeed = ArrayUtilities.getRandom(n);
    this.needs.put(lowNeed, LOW_NEED_VALUE);
    ArrayUtilities.remove(n, lowNeed);
  }

  private static int getGroupId() {
    // if a group is full, open up the next one
    if (currentGroupSize >= MAX_GROUP_SIZE) {

      return createNewGroup();
    }

    if (MathUtilities.probabilityIsTrue(currentGroupProbability)) {
      currentGroupSize++;
      currentGroupProbability /= Math.sqrt(2);
      return currentGroupId;
    }

    return createNewGroup();
  }

  private static int createNewGroup() {
    currentGroupId++;
    currentGroupSize = 0;
    currentGroupProbability = 1;
    return currentGroupId;
  }
}
