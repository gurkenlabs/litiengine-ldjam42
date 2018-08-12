package de.gurkenlabs.ldjam42.gui;

import java.awt.Graphics2D;
import java.text.SimpleDateFormat;

import de.gurkenlabs.ldjam42.ClubArea;
import de.gurkenlabs.ldjam42.GameManager;
import de.gurkenlabs.ldjam42.Program;
import de.gurkenlabs.ldjam42.entities.PartyGuest;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.TextRenderer;
import de.gurkenlabs.litiengine.gui.GuiComponent;
import de.gurkenlabs.litiengine.util.MathUtilities;

public final class Hud extends GuiComponent {

  public Hud() {
    super(0, 0, Game.getScreenManager().getResolution().getWidth(), Game.getScreenManager().getResolution().getHeight());
  }

  @Override
  public void render(final Graphics2D g) {
    this.renderCurrentFocus(g);
    this.renderAreaInfo(g);
    this.renderTime(g);
    this.renderMoney(g);

    super.render(g);
  }

  private void renderCurrentFocus(Graphics2D g) {
    if (GameManager.getCurrentFocus() != null) {
      PartyGuest guest = GameManager.getCurrentFocus();
      // render info/ image of currently focused guest
      String info = guest.getMapId() + " " + guest.getSatisfaction();
      double height = g.getFontMetrics().getHeight();
      double y = Game.getScreenManager().getResolution().getHeight() - height * 2;
      double guestWidth = g.getFontMetrics().stringWidth(info);
      TextRenderer.render(g, info, Game.getScreenManager().getResolution().getWidth() / 2.0 - guestWidth / 2, y);
    }
  }

  private void renderAreaInfo(Graphics2D g) {
    int dance = GameManager.getGuests(ClubArea.DANCEFLOOR);
    int pizza = GameManager.getGuests(ClubArea.PIZZASTAND);
    int bar = GameManager.getGuests(ClubArea.BAR);
    int chill = GameManager.getGuests(ClubArea.CHILLAREA);
    double total = GameManager.getTotalGuestsInMainAreas();
    TextRenderer.render(g, "dance: " + dance + " " + MathUtilities.getFullPercent(total, dance), 200, 250);
    TextRenderer.render(g, "food: " + pizza + " " + MathUtilities.getFullPercent(total, pizza), 200, 300);
    TextRenderer.render(g, "drink: " + bar + " " + MathUtilities.getFullPercent(total, bar), 200, 350);
    TextRenderer.render(g, "chill: " + chill + " " + MathUtilities.getFullPercent(total, chill), 200, 400);
  }

  private void renderTime(Graphics2D g) {
    SimpleDateFormat form = new SimpleDateFormat("HH:mm");
    String time = form.format(GameManager.getCurrentGameTime());

    g.setFont(Program.GUI_FONT);
    TextRenderer.render(g, time, 200, 200);
  }

  private void renderMoney(Graphics2D g) {
    String money = GameManager.getCurrentMoney() + "$";
    double width = g.getFontMetrics().stringWidth(money);
    double height = g.getFontMetrics().getHeight();
    double moneyY = Game.getScreenManager().getResolution().getHeight() - height * 2;
    TextRenderer.render(g, money, Game.getScreenManager().getResolution().getWidth() / 2.0 - width / 2, moneyY);
  }
}
