package de.gurkenlabs.ldjam42.entities;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.annotation.CollisionInfo;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.annotation.MovementInfo;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.graphics.animation.EntityAnimationController;
import de.gurkenlabs.litiengine.physics.MovementController;

@MovementInfo(velocity = 50)
@EntityInfo(width = 11, height = 22)
@CollisionInfo(collision = true, collisionBoxWidth = 11, collisionBoxHeight = 11)
public class PartyGuest extends Creature {
  public PartyGuest(Point2D location) {
    this.setLocation(location);
    // TODO: evaluate random apperance
    this.setController(EntityAnimationController.class, new PartyGuestAnimationController(this));
    this.setController(MovementController.class, new PartyGuestController(this));
  }

  @Override
  public boolean canCollideWith(final ICollisionEntity otherEntity) {
    if (otherEntity instanceof PartyGuest) {
      return false;
    }

    return true;
  }
}
