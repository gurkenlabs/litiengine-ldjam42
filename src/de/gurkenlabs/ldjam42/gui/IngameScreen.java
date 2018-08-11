package de.gurkenlabs.ldjam42.gui;

import java.awt.Graphics2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.gui.screens.Screen;

public class IngameScreen extends Screen {
  public static final String NAME = "INGAME";

  public IngameScreen() {
    super(NAME);
  }

  @Override
  public void render(final Graphics2D g) {
    if (Game.getEnvironment() != null) {
      Game.getEnvironment().render(g);
    }

    super.render(g);
  }
}
