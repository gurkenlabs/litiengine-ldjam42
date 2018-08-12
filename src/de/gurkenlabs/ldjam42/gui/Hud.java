package de.gurkenlabs.ldjam42.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.sql.Time;
import java.text.SimpleDateFormat;

import de.gurkenlabs.ldjam42.ClubArea;
import de.gurkenlabs.ldjam42.GameManager;
import de.gurkenlabs.ldjam42.Program;
import de.gurkenlabs.ldjam42.entities.PartyGuest;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.ImageRenderer;
import de.gurkenlabs.litiengine.graphics.ShapeRenderer;
import de.gurkenlabs.litiengine.graphics.TextRenderer;
import de.gurkenlabs.litiengine.gui.GuiComponent;
import de.gurkenlabs.litiengine.gui.ImageComponent;
import de.gurkenlabs.litiengine.util.MathUtilities;

public final class Hud extends GuiComponent {
  private static final Color COLOR_MONEY = new Color(77, 125, 10);
  private static final Color COLOR_MONEY_BORDER = new Color(11, 240, 10, 50);
  private ImageComponent kickButton;

  public Hud() {
    super(0, 0, Game.getScreenManager().getResolution().getWidth(), Game.getScreenManager().getResolution().getHeight());
  }

  @Override
  public void render(final Graphics2D g) {
    this.renderCurrentFocus(g);
    this.renderTime(g);
    this.renderMoney(g);

    super.render(g);
  }

  public void showDismissButton() {
    this.kickButton.setVisible(true);
  }

  public void hideDismissButton() {
    this.kickButton.setVisible(false);
  }

  @Override
  public void prepare() {
    super.prepare();
    this.kickButton.setVisible(true);
    this.kickButton.onMousePressed(e -> {
      GameManager.dismiss();
    });
  }

  @Override
  protected void initializeComponents() {
    super.initializeComponents();

    double width = Game.getScreenManager().getResolution().getWidth() / 10;
    double height = Game.getScreenManager().getResolution().getHeight() / 10;
    this.kickButton = new ImageComponent(Game.getScreenManager().getResolution().getWidth() / 2.0 - width / 2.0, Game.getScreenManager().getResolution().getHeight() / 2 + height, width, height);
    this.kickButton.setText("DISMISS");
    this.kickButton.getAppearance().setForeColor(new Color(0, 0, 0, 220));
    this.kickButton.getAppearance().setBackgroundColor1(new Color(255, 0, 0, 150));
    this.kickButton.getAppearance().setTransparentBackground(false);
    this.kickButton.getAppearanceHovered().setForeColor(new Color(0, 0, 0, 255));
    this.kickButton.getAppearanceHovered().setBackgroundColor1(new Color(255, 0, 0, 190));
    this.kickButton.getAppearanceHovered().setTransparentBackground(false);

    this.getComponents().add(this.kickButton);
  }

  private void renderCurrentFocus(Graphics2D g) {
    if (GameManager.getCurrentFocus() == null) {
      return;
    }

    PartyGuest guest = GameManager.getCurrentFocus();

    BufferedImage image = guest.getAnimationController().getCurrentSprite();
    double factor = 150 / image.getWidth();
    double imgWidth = factor * image.getWidth();
    double imgHeight = factor * image.getHeight();
    double imgX = Game.getScreenManager().getResolution().getWidth() / 2.0 - imgWidth / 2.0;
    double imgY = this.kickButton.getY() + this.kickButton.getHeight() + imgHeight * 0.1;
    ImageRenderer.renderScaled(g, guest.getAnimationController().getCurrentSprite(), imgX, imgY, factor);

    g.setFont(Program.GUI_FONT_SMALL);
    Time clubbingSince = new Time(GameManager.getStartTime().getTime() + Game.getLoop().convertToMs(guest.getClubbingSince()) * 60);
    // render info/ image of currently focused guest
    SimpleDateFormat form = new SimpleDateFormat("HH:mm");
    String time = "Clubbing since " + form.format(clubbingSince);
    double width = g.getFontMetrics().stringWidth(time);
    double y = Game.getScreenManager().getResolution().getHeight() - g.getFontMetrics().getHeight() * 0.8;
    TextRenderer.render(g, time, Game.getScreenManager().getResolution().getWidth() / 2.0 - width / 2, y);

    // name bg
    g.setFont(Program.GUI_FONT_SMALL.deriveFont(36f));
    double nameWidth = g.getFontMetrics().stringWidth(guest.getName());
    double nameX = Game.getScreenManager().getResolution().getWidth() / 2.0 - nameWidth / 2;
    double nameY = y - g.getFontMetrics().getHeight();
    final int PADDING = 10;
    Rectangle2D nameBg = new Rectangle2D.Double(nameX - PADDING, nameY - g.getFontMetrics().getHeight(), nameWidth + 2 * PADDING, g.getFontMetrics().getHeight() + PADDING);
    g.setColor(new Color(0, 0, 0, 150));
    ShapeRenderer.render(g, nameBg);

    g.setColor(Color.WHITE);
    TextRenderer.render(g, guest.getName(), nameX, nameY);
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
    g.setColor(new Color(0, 0, 0, 220));

    g.setFont(Program.GUI_FONT.deriveFont(80f));
    String unit = "$";
    double unitWidth = g.getFontMetrics().stringWidth(unit);
    double unitHeight = g.getFontMetrics().getHeight();
    double unitY = unitHeight * 1.1;
    g.setFont(Program.GUI_FONT.deriveFont(80f));
    TextRenderer.renderWithOutline(g, unit, Game.getScreenManager().getResolution().getWidth() / 2.0 - unitWidth / 2, unitY, COLOR_MONEY_BORDER, RenderingHints.VALUE_ANTIALIAS_ON);

    g.setFont(Program.GUI_FONT);
    String money = GameManager.getCurrentMoney() + "";
    double width = g.getFontMetrics().stringWidth(money);
    double height = g.getFontMetrics().getHeight();
    double moneyY = height + unitY * 1.1;
    g.setFont(Program.GUI_FONT);
    TextRenderer.renderWithOutline(g, money, Game.getScreenManager().getResolution().getWidth() / 2.0 - width / 2, moneyY, COLOR_MONEY_BORDER, RenderingHints.VALUE_ANTIALIAS_ON);
  }
}
