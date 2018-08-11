package de.gurkenlabs.ldjam42.entities;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.entities.Creature;

public class PartyGuest extends Creature {
  public PartyGuest(Point2D location) {
    this.setLocation(location);

    // TODO: evaluate random apperance
    this.getControllers().setController(PartyGuestAnimationController.class, new PartyGuestAnimationController(this));
  }
}
