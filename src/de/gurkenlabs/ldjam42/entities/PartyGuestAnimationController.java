package de.gurkenlabs.ldjam42.entities;

import de.gurkenlabs.ldjam42.graphics.FocusImageEffect;
import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.Animation;
import de.gurkenlabs.litiengine.graphics.animation.CreatureAnimationController;

public class PartyGuestAnimationController extends CreatureAnimationController<PartyGuest> {

  private static String[] states = { "walk", "idle" };

  public PartyGuestAnimationController(PartyGuest entity) {
    super(entity, new Animation(Spritesheet.find("dummy"), true));
    for (String state : states) {
      for (Direction dir : Direction.values()) {
        String spritePath = String.format("%s-%s-%s", entity.getSpritePrefix(), state, dir.toString().toLowerCase());
        Spritesheet sprite = Spritesheet.find(spritePath);
        String nakedSpritePath = String.format("%s-%d_%d_%d_%d-%s-%s", entity.getGender().toString().toLowerCase(), 0, 0, entity.getFeatures()[2], entity.getFeatures()[3], state, dir.toString().toLowerCase());
        Spritesheet nakedSprite = Spritesheet.find(nakedSpritePath);

        if (sprite == null || nakedSprite == null) {
          continue;
        }
        this.add(new Animation(sprite, true, 200, 200));
        this.add(new Animation(nakedSprite, true, 200, 200));
      }
    }

    this.add(new FocusImageEffect(entity));
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
