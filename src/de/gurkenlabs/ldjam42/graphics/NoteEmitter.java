package de.gurkenlabs.ldjam42.graphics;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.annotation.EmitterInfo;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.emitters.SpritesheetEmitter;
import de.gurkenlabs.litiengine.graphics.emitters.particles.Particle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.SpriteParticle;
import de.gurkenlabs.litiengine.physics.CollisionType;
import de.gurkenlabs.litiengine.util.MathUtilities;

@EmitterInfo(maxParticles = 3, emitterTTL = 1000, particleMinTTL = 500, particleMaxTTL = 1000, activateOnInit = false)
@EntityInfo(renderType = RenderType.OVERLAY)
public class NoteEmitter extends SpritesheetEmitter {

  public NoteEmitter(Point2D origin) {
    super(Spritesheet.find("note"), origin);
  }

  @Override
  public Particle createNewParticle() {
    final int life = this.getRandomParticleTTL();
    final int x = (int) (2 * Math.random() * MathUtilities.randomSign());
    final int y = (int) (2 * Math.random()
        * MathUtilities.randomSign());
    final float gravityX = 0.05f * MathUtilities.randomSign();
    final float gravityY = -0.05f * MathUtilities.randomSign();

    final SpriteParticle p = new SpriteParticle(this.getRandomSprite(), life);
    p.setX(x);
    p.setY(y);
    p.setDeltaIncX(gravityX);
    p.setDeltaIncY(gravityY);
    p.setCollisionType(CollisionType.NONE);
    p.setDeltaX((float) MathUtilities.randomInRange(-1.0, 1));
    p.setDeltaY((float) MathUtilities.randomInRange(-1, -0.5));
    return p;
  }
}
