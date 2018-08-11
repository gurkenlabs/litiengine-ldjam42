package de.gurkenlabs.ldjam42.entities;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import de.gurkenlabs.ldjam42.Program;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.annotation.CollisionInfo;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.annotation.MovementInfo;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.graphics.DebugRenderer;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.TextRenderer;
import de.gurkenlabs.litiengine.graphics.animation.EntityAnimationController;
import de.gurkenlabs.litiengine.physics.MovementController;
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

  private void initialize() {
    this.group = getGroupId();
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
