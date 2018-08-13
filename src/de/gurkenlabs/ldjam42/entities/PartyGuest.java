package de.gurkenlabs.ldjam42.entities;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Optional;

import de.gurkenlabs.ldjam42.BadBehavior;
import de.gurkenlabs.ldjam42.ClubArea;
import de.gurkenlabs.ldjam42.GameManager;
import de.gurkenlabs.ldjam42.graphics.DismissEmitter;
import de.gurkenlabs.ldjam42.graphics.FlashEmitter;
import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.annotation.CollisionInfo;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.annotation.MovementInfo;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.entities.MapArea;
import de.gurkenlabs.litiengine.graphics.DebugRenderer;
import de.gurkenlabs.litiengine.graphics.animation.EntityAnimationController;
import de.gurkenlabs.litiengine.physics.MovementController;
import de.gurkenlabs.litiengine.sound.Sound;
import de.gurkenlabs.litiengine.util.ArrayUtilities;
import de.gurkenlabs.litiengine.util.MathUtilities;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;

@MovementInfo(velocity = 50)
@EntityInfo(width = 11, height = 22)
@CollisionInfo(collision = true, collisionBoxWidth = 11, collisionBoxHeight = 11, align = Align.CENTER, valign = Valign.MIDDLE)
public class PartyGuest extends Creature {

  public static Sound DISMISS_MALE = Sound.get("dismiss-male.ogg");
  public static Sound DISMISS_FEMALE = Sound.get("dismiss-female.ogg");

  public static final double OCCUPATION = 16;
  private static final int WEALTH_DEFAULT = 1;
  private static final int WEALTH_VIP = 10;
  private static final double COMFORT_ZONE_WEIGHT = 4;
  private static final double REMAINING_SPACE_WEIGHT = 0.5;
  private static final double BAD_BEHAVIOR_PENALTY = 3;

  private static final String[] femaleNames;
  private static final String[] maleNames;

  private static Group currentGroup;

  private Group group;
  private int[] features;
  private BadBehavior badBehavior;
  private final Gender gender;
  private State state;
  private int wealth;
  private double satisfaction;
  private ClubArea currentArea;

  private long clubbingSince;

  static {
    DebugRenderer.addEntityDebugListener((g, e) -> {
    });

    femaleNames = Resources.getStringList("names-female.txt");
    maleNames = Resources.getStringList("names-male.txt");
  }

  public PartyGuest(Point2D location) {
    this.gender = MathUtilities.randomBoolean() ? Gender.FEMALE : Gender.MALE;

    this.setName(ArrayUtilities.getRandom(this.getGender() == Gender.FEMALE ? femaleNames : maleNames));
    this.satisfaction = 1;
    this.features = new int[4];
    this.setLocation(location);
    // TODO: evaluate random apperance
    this.setVelocity((short) MathUtilities.randomInRange(20, 30));
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
    this.clubbingSince = Game.getLoop().getTicks();
  }

  public Ellipse2D getComfortZone() {
    return new Ellipse2D.Double(this.getCenter().getX() - 20, this.getCenter().getY() - 20, 40, 40);
  }

  public Group getGroup() {
    return this.group;
  }

  public int[] getFeatures() {
    return this.features;
  }

  @Override
  public Direction getFacingDirection() {
    float angle = this.getAngle();
    if (angle >= 0 && angle < 60) {
      return Direction.DOWN;
    }
    if (angle >= 60 && angle < 120) {
      return Direction.RIGHT;
    }
    if (angle >= 120 && angle < 240) {
      return Direction.UP;
    }
    if (angle >= 240 && angle < 300) {
      return Direction.LEFT;
    }

    if (angle >= 300 && angle <= 360) {
      return Direction.DOWN;
    }

    return Direction.UNDEFINED;
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

  public long getClubbingSince() {
    return this.clubbingSince;
  }

  public boolean isNaked() {
    return this.getFeatures()[Feature.PANTS.ordinal()] == 0 || this.getFeatures()[Feature.TOP.ordinal()] == 0;
  }

  public boolean isCompletelyNaked() {
    return this.getFeatures()[Feature.PANTS.ordinal()] == 0 && this.getFeatures()[Feature.TOP.ordinal()] == 0;
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
    double comfort = 1 / Math.sqrt(guestsInComfortZone == 0 ? 1.0 : (double) guestsInComfortZone);

    this.satisfaction = (remainingSpace * REMAINING_SPACE_WEIGHT + comfort * COMFORT_ZONE_WEIGHT) / (COMFORT_ZONE_WEIGHT + REMAINING_SPACE_WEIGHT);

    if (!this.isNaked() && this.nakedGuestsInComfortZone() > 0) {
      this.satisfaction /= BAD_BEHAVIOR_PENALTY * this.nakedGuestsInComfortZone();
    }
  }

  public void flash() {
    Game.getEnvironment().add(new FlashEmitter(this));

    this.features[0] = 0;
    this.features[1] = 0;
    this.setVelocity((short) MathUtilities.randomInRange(30, 50));
//    System.out.println(String.format("Someone named %s (%s) is showing what they've got.", this.getName(), this.getSpritePrefix()));
  }

  public boolean isFlashing() {
    return (this.getFeatures()[0] == 0 && this.getFeatures()[1] == 0);
  }

  @Override
  public String getSpritePrefix() {
    if (this.features != null && this.getGender() != null) {
      String prefix = (String.format("%s-%d_%d_%d_%d", this.getGender().toString().toLowerCase(), this.getFeatures()[0], this.getFeatures()[1], this.getFeatures()[2], this.getFeatures()[3]));
      return prefix;
    }
    return super.getSpritePrefix();
  }

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

  private int nakedGuestsInComfortZone() {
    int cnt = 0;
    for (PartyGuest g : Game.getEnvironment().getByType(PartyGuest.class)) {
      if (g == null || g.equals(this)) {
        continue;
      }

      if (GeometricUtilities.intersects(g.getComfortZone(), this.getComfortZone())) {
        if (g.isCompletelyNaked()) {
          cnt += 3;
        } else if (g.isNaked()) {
          cnt++;
        }
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
    this.initializeWealth();

    this.group = this.assignGroup();
    this.initializeFeatures();

    this.setSpritePrefix(String.format("%s-%d_%d_%d_%d", this.getGender().toString().toLowerCase(), this.getFeatures()[0], this.getFeatures()[1], this.getFeatures()[2], this.getFeatures()[3]));
    this.setController(EntityAnimationController.class, new PartyGuestAnimationController(this));
  }

  private void initializeFeatures() {
    for (Feature feature : Feature.values()) {
      // group features
      if (ArrayUtilities.contains(this.getGroup().getFeatures(), feature)) {
        this.features[feature.ordinal()] = this.getGroup().getFeature(feature);
      } else {
        this.features[feature.ordinal()] = feature.getRandomValue();
      }
    }
  }

  private void initializeWealth() {
    this.wealth = WEALTH_DEFAULT;
    // TODO: implement VIP WEALTH_VIP
  }

  private Group assignGroup() {
    // if a group is full, open up the next one
    if (currentGroup == null || currentGroup.getSize() >= Group.MAX_GROUP_SIZE) {

      return new Group();
    }

    if (MathUtilities.probabilityIsTrue(currentGroup.getProbability())) {
      currentGroup.setProbability(currentGroup.getProbability() / Math.sqrt(2));
      currentGroup.add(this);
      return currentGroup;
    }

    return new Group();
  }
}
