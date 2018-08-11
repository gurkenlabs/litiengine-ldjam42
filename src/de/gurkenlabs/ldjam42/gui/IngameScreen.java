package de.gurkenlabs.ldjam42.gui;

import java.awt.Graphics2D;
import java.text.SimpleDateFormat;

import de.gurkenlabs.ldjam42.ClubArea;
import de.gurkenlabs.ldjam42.GameManager;
import de.gurkenlabs.ldjam42.Program;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.TextRenderer;
import de.gurkenlabs.litiengine.gui.screens.Screen;
import de.gurkenlabs.litiengine.util.MathUtilities;

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
    int dance = GameManager.getGuests(ClubArea.DANCEFLOOR);
    int pizza = GameManager.getGuests(ClubArea.PIZZASTAND);
    int bar = GameManager.getGuests(ClubArea.BAR);
    int chill = GameManager.getGuests(ClubArea.CHILLAREA);
    double total = GameManager.getTotalGuestsInMainAreas();
    TextRenderer.render(g, "dance: " + dance + " " + MathUtilities.getFullPercent(total, dance), 200, 250);
    TextRenderer.render(g, "food: " + pizza + " " + MathUtilities.getFullPercent(total, pizza), 200, 300);
    TextRenderer.render(g, "drink: " + bar + " " + MathUtilities.getFullPercent(total, bar), 200, 350);
    TextRenderer.render(g, "chill: " + chill + " " + MathUtilities.getFullPercent(total, chill), 200, 400);

    String money = GameManager.getCurrentMoney() + "$";
    double width = g.getFontMetrics().stringWidth(money);
    double height = g.getFontMetrics().getHeight();
    TextRenderer.render(g, money, Game.getScreenManager().getResolution().getWidth() / 2.0 - width / 2, Game.getScreenManager().getResolution().getHeight() - height * 2);
    super.render(g);
  }
}
