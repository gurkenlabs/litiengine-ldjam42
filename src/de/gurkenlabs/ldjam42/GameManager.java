package de.gurkenlabs.ldjam42;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.IEnvironment;

public final class GameManager {
  private static IEnvironment goin;

  private GameManager() {
  }

  public static void init() {
    goin = new Environment(Game.getMap("test"));
  }

  public static IEnvironment getGoin() {
    return goin;
  }

}
