package de.gurkenlabs.ldjam42.entities;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.Animation;
import de.gurkenlabs.litiengine.graphics.animation.CreatureAnimationController;
import de.gurkenlabs.litiengine.graphics.emitters.SpritesheetEntityEmitter;

public class PartyGuestAnimationController extends CreatureAnimationController<PartyGuest> {
  private static final String PANTS = "pants";
  private static final String TOP = "top";
  private static final String FACE = "face";
  private static final String HAIR = "hair";

  private static String GENDER;

  public PartyGuestAnimationController(PartyGuest entity) {
    super(entity, new Animation(Spritesheet.find("dummy"), true));
    this.GENDER = entity.getGender().toString().toLowerCase();
    // add clothing array from partyGuest
    for(int i = 0; i<=3;i++) {
      
    }
//    this.add(new Animation(, true, 200));
  }

  @Override
  public Animation getCurrentAnimation() {
    // TODO Auto-generated method stub
    return super.getCurrentAnimation();
  }

  @Override
  public Animation getDefaultAnimation() {
    // TODO Auto-generated method stub
    return super.getDefaultAnimation();
  }

}
