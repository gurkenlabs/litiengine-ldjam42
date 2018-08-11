package de.gurkenlabs.ldjam42.gui;

import java.awt.Graphics2D;
import java.text.SimpleDateFormat;

import de.gurkenlabs.ldjam42.GameManager;
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

    SimpleDateFormat form = new SimpleDateFormat("HH:mm");
    String time = form.format(GameManager.getCurrentGameTime());

    g.setFont(Program.GUI_FONT);
    TextRenderer.render(g, time, 200, 200);
    TextRenderer.render(g, "dance: " + GameManager.getGuestsInDanceAreas(), 200, 250);
    TextRenderer.render(g, "food: " + GameManager.getGuestsInFoodAreas(), 200, 300);
    TextRenderer.render(g, "drink: " + GameManager.getGuestsInDrinkAreas(), 200, 350);
    TextRenderer.render(g, "chill: " + GameManager.getGuestsInChillAreas(), 200, 400);
    super.render(g);
  }
}
