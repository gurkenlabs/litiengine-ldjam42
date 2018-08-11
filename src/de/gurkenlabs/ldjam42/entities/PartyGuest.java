package de.gurkenlabs.ldjam42.entities;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;
import java.util.EnumMap;
import java.util.Map;

import de.gurkenlabs.ldjam42.BadBehavior;
import de.gurkenlabs.ldjam42.Needs;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.annotation.CollisionInfo;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.annotation.MovementInfo;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.ICollisionEntity;
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
  private static final int MAX_GROUP_SIZE = 9;
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

  static {
    DebugRenderer.addEntityDebugListener((g, e) -> {
      if (e instanceof PartyGuest) {
        PartyGuest guest = (PartyGuest) e;
        g.setColor(Color.BLACK);
        g.setFont(g.getFont().deriveFont(Font.BOLD, 4f));
        final int x = (int) Game.getCamera().getViewPortDimensionCenter(e).getX();
        final int y = (int) Game.getCamera().getViewPortDimensionCenter(e).getY() - 10;
        TextRenderer.render(g, Integer.toString(guest.getGroup()), x, y);
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

  public int getGroup() {
    return this.group;
  }

  public Map<Needs, Integer> getNeeds() {
    return this.needs;
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
    final int HIGHEST_NEED_VALUE = 3;
    final int AVG_NEED_VALUE = 2;
    final int LOW_NEED_VALUE = 1;

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
