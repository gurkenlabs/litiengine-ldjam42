package de.gurkenlabs.ldjam42.entities;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.Animation;
import de.gurkenlabs.litiengine.graphics.animation.CreatureAnimationController;

public class PartyGuestAnimationController extends CreatureAnimationController<PartyGuest> {

  public PartyGuestAnimationController(PartyGuest entity) {
    super(entity, new Animation(Spritesheet.find("dummy"), true));
  }

}
