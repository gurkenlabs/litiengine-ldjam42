package de.gurkenlabs.ldjam42.entities;

import java.util.Random;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.physics.MovementController;

public class PartyGuestController extends MovementController<PartyGuest> {
  private static final int ANGLE_CHANGE_MIN_DELAY = 2000;
  private static final int ANGLE_CHANGE_RANDOM_DELAY = 3000;
  private static final Random RANDOM = new Random();
  
  private int angle;

 private long lastAngleChange;
  private long nextAngleChange;

  public PartyGuestController(PartyGuest mobileEntity) {
    super(mobileEntity);
  }

  @Override
  public void update() {
    super.update();
    
    this.walkAroundLikeMotherfucker();
  }
  
  protected void walkAroundLikeMotherfucker() {
    // WALK AROUND LIKE MOTHERFUCKERS
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
