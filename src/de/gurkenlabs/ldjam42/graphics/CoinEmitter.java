package de.gurkenlabs.ldjam42.graphics;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.annotation.EmitterInfo;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.emitters.SpritesheetEmitter;
import de.gurkenlabs.litiengine.graphics.emitters.particles.Particle;

@EmitterInfo(activateOnInit = true, emitterTTL = 1200, maxParticles = 1, spawnAmount = 1, particleMaxTTL = 1200, particleMinTTL = 1200)
@EntityInfo(renderType = RenderType.OVERLAY)
public class CoinEmitter extends SpritesheetEmitter {

  public CoinEmitter(Point2D origin) {
    super(Spritesheet.find("coin"), new Point2D.Double(origin.getX() - 5, origin.getY() - 10));
  }

  @Override
  protected Particle createNewParticle() {

    final Particle p = super.createNewParticle();
    //p.setFade(false);
    p.setDeltaY(-0.7f);
    return p;
  }

}
