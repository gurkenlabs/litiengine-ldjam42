package de.gurkenlabs.ldjam42.entities;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Optional;
import java.util.stream.IntStream;

import de.gurkenlabs.ldjam42.BadBehavior;
import de.gurkenlabs.ldjam42.ClubArea;
import de.gurkenlabs.ldjam42.GameManager;
import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Valign;
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
import de.gurkenlabs.litiengine.util.MathUtilities;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;

@MovementInfo(velocity = 50)
@EntityInfo(width = 11, height = 22)
@CollisionInfo(collision = true, collisionBoxWidth = 11, collisionBoxHeight = 11, align = Align.CENTER, valign = Valign.MIDDLE)
public class PartyGuest extends Creature {
  public static final double OCCUPATION = 20;
  private static final int MAX_GROUP_SIZE = 9;
  private static final int DEFAULT_WEALTH_MIN = 1;
  private static final int DEFAULT_WEALTH_MAX = 5;
  private static final double COMFORT_ZONE_WEIGHT = 1;
  private static final double REMAINING_SPACE_WEIGHT = 2;

  private static int currentGroupId;
  private static double currentGroupProbability = 1;
  private static int currentGroupSize;
  private static int[] currentGroupFeatures = new int[2], currentGroupFeatureIndices = new int[2];

  private int group;
  private int[] features;
  private BadBehavior badBehavior;
  private final Gender gender;
  private State state;
  private int wealth;
  private double satisfaction;
  private ClubArea currentArea;

  static {
    DebugRenderer.addEntityDebugListener((g, e) -> {
      if (e instanceof PartyGuest) {
        PartyGuest guest = (PartyGuest) e;
        g.setColor(Color.BLACK);
        g.setFont(g.getFont().deriveFont(Font.BOLD, 4f));
        final int x = (int) Game.getCamera().getViewPortDimensionCenter(e).getX();
        final int y = (int) Game.getCamera().getViewPortDimensionCenter(e).getY() - 10;
        TextRenderer.render(g, (int) (guest.getSatisfaction() * 100) + "%", x, y);
        TextRenderer.render(g, guest.getState() != null ? guest.getState().toString() : "", x, y + 10);
        Game.getRenderEngine().renderOutline(g, guest.getComfortZone());
      }
    });
  }

  public PartyGuest(Point2D location) {
    this.gender = MathUtilities.randomBoolean() ? Gender.FEMALE : Gender.MALE;
    this.satisfaction = 1;
    this.features = new int[4];
    this.setLocation(location);
    // TODO: evaluate random apperance
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
  }

  public Ellipse2D getComfortZone() {
    return new Ellipse2D.Double(this.getCenter().getX() - 20, this.getCenter().getY() - 20, 40, 40);
  }

  public int getGroup() {
    return this.group;
  }

  public int[] getFeatures() {
    return this.features;
  }

  public ClubArea getCurrentArea() {
    return this.currentArea;
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

    // update by value for the area the guest is currently in
    ClubArea current = this.getCurrentArea() != null ? this.getCurrentArea() : ClubArea.LOBBY;

    double remainingSpace = GameManager.getRemainingSpace(current);

    long guestsInComfortZone = this.getGuestsInComfortZone();
    double comfort = 1 / (guestsInComfortZone == 0 ? 1.0 : (double) guestsInComfortZone);

    this.satisfaction = (remainingSpace * REMAINING_SPACE_WEIGHT + comfort * COMFORT_ZONE_WEIGHT) / (COMFORT_ZONE_WEIGHT + REMAINING_SPACE_WEIGHT);
  }

//  @Override
//  public String getSpritePrefix() {
//    if (this.getFeatures() == null) {
//      return super.getSpritePrefix();
//    }
//    String prefix = 
//    return prefix;
//  }

  private int getGuestsInComfortZone() {
    int cnt = 0;
    for (PartyGuest g : Game.getEnvironment().getByType(PartyGuest.class)) {
      if (g == null || g.equals(this)) {
        continue;
      }

      if (GeometricUtilities.intersects(g.getComfortZone(), this.getComfortZone())) {
        cnt++;
      }
    }
    return cnt;
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
    int featureNumber = 0;
    for (int i = 0; i <= 3; i++) {
      final int tmp = i;
      if (IntStream.of(currentGroupFeatureIndices).anyMatch(x -> x == tmp)) {
        this.features[tmp] = currentGroupFeatures[featureNumber];
      } else {
        this.features[i] = MathUtilities.randomInRange(0, 4);
      }
    }
    this.setSpritePrefix(String.format("%s-%d_%d_%d_%d", this.getGender().toString().toLowerCase(), this.getFeatures()[0], this.getFeatures()[1], this.getFeatures()[2], this.getFeatures()[3]));
    this.initializeWealth();
    this.setController(EntityAnimationController.class, new PartyGuestAnimationController(this));

  }

  private void initializeWealth() {
    this.wealth = MathUtilities.randomInRange(DEFAULT_WEALTH_MIN, DEFAULT_WEALTH_MAX);
    // TODO: implement VIP
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
    currentGroupFeatureIndices[0] = MathUtilities.randomInRange(0, 4);
    currentGroupFeatureIndices[1] = MathUtilities.randomInRange(0, 4);
    while (currentGroupFeatureIndices[0] == currentGroupFeatureIndices[1]) {
      currentGroupFeatureIndices[1] = MathUtilities.randomInRange(0, 4);
    }

    currentGroupFeatures[0] = MathUtilities.randomInRange(0, 4);
    currentGroupFeatures[1] = MathUtilities.randomInRange(0, 4);

    return currentGroupId;
  }
}
