package de.gurkenlabs.ldjam42.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import de.gurkenlabs.ldjam42.Program;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.TextRenderer;
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

    g.setColor(Color.RED);
    g.setFont(Program.GUI_FONT);
    TextRenderer.render(g, "OHHHH BOY!", new Point2D.Double(150, 150));
    super.render(g);
  }
}
