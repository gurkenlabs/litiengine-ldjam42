package de.gurkenlabs.ldjam42.entities;

import java.util.Random;

import de.gurkenlabs.ldjam42.GameManager;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.physics.MovementController;
import de.gurkenlabs.litiengine.util.MathUtilities;

public class PartyGuestController extends MovementController<PartyGuest> {

  private static final Random RANDOM = new Random();

  // State.WALK
  private static final int ANGLE_CHANGE_MIN_DELAY = 2000;
  private static final int ANGLE_CHANGE_RANDOM_DELAY = 3000;

  private int angle;
  private long lastAngleChange;
  private long nextAngleChange;

  // STATE MONEY
  private static final int PAY_MIN_SPEND = 5;
  private static final int PAY_MIN_INTERVAL = 5000;
  private static final int PAY_MAX_INTERVAL = 10000;
  private int paymentInterval;
  private long lastPayment;

  // STATE IDLE

  public PartyGuestController(PartyGuest mobileEntity) {
    super(mobileEntity);
  }

  @Override
  public void update() {
    super.update();

    this.updateState();

    switch (this.getEntity().getState()) {
    case IDLE:
      break;
    case BAD_BEHAVIOR:
      break;
    case CHANGE_AREA:
      break;
    case SPEND_MONEY:
      this.spendMoney();
      break;
    case WALK:
    default:
      this.walkAround();
      break;
    }
  }

  private void updateState() {

    if (Game.getLoop().getDeltaTime(this.lastPayment) > paymentInterval) {
      this.getEntity().setState(State.SPEND_MONEY);
      return;
    }

    this.getEntity().setState(State.WALK);
  }

  private void spendMoney() {
    if(this.getEntity().getSatisfaction() == 0) {
      this.paymentInterval = MathUtilities.randomInRange(PAY_MIN_INTERVAL, PAY_MAX_INTERVAL);
      this.lastPayment = Game.getLoop().getTicks();
      return;
    }
    
    int money = Math.round(PAY_MIN_SPEND + (float) Math.pow(this.getEntity().getWealth(), 2));
    GameManager.spendMoney(money);
    this.paymentInterval = MathUtilities.randomInRange(PAY_MIN_INTERVAL, PAY_MAX_INTERVAL);
    this.lastPayment = Game.getLoop().getTicks();
  }

  protected void walkAround() {
    float pixelsPerTick = this.getEntity().getTickVelocity();
    final long currentTick = Game.getLoop().getTicks();
    final long timeSinceLastAngleChange = Game.getLoop().getDeltaTime(this.lastAngleChange);
    if (this.angle == 0 || timeSinceLastAngleChange > this.nextAngleChange) {
      final Random rand = new Random();
      this.angle = rand.nextInt(360);
      this.lastAngleChange = currentTick;
      this.calculateNextAngleChange();
    }
    this.getEntity().setAngle(this.angle);
    Game.getPhysicsEngine().move(this.getEntity(), this.getEntity().getAngle(), pixelsPerTick);
  }

  private void calculateNextAngleChange() {
    this.nextAngleChange = RANDOM.nextInt(ANGLE_CHANGE_RANDOM_DELAY) + ANGLE_CHANGE_MIN_DELAY;
  }
}
