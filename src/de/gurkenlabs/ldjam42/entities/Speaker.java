package de.gurkenlabs.ldjam42.entities;

import de.gurkenlabs.ldjam42.graphics.NoteEmitter;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.annotation.AnimationInfo;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.environment.EnvironmentAdapter;
import de.gurkenlabs.litiengine.environment.IEnvironment;

@AnimationInfo(spritePrefix = "prop-speakers")
public class Speaker extends Prop {

  public Speaker(String spritesheetName) {
    super(spritesheetName);
  }

  @Override
  public void loaded() {
    super.loaded();
    NoteEmitter emitter = new NoteEmitter(this.getCenter());
    Game.getEnvironment().add(emitter);
    
    // dunno why that's necessary, maybe the emitter is added too soon?
    Game.getEnvironment().addListener(new EnvironmentAdapter() {
      @Override
      public void environmentLoaded(IEnvironment environment) {
        super.environmentLoaded(environment);
        Game.getLoop().attach(emitter);
      }
    });
  }
}
