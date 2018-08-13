package de.gurkenlabs.ldjam42.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Locale;

import de.gurkenlabs.ldjam42.ClubArea;
import de.gurkenlabs.ldjam42.GameManager;
import de.gurkenlabs.ldjam42.Program;
import de.gurkenlabs.ldjam42.entities.PartyGuest;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.ImageRenderer;
import de.gurkenlabs.litiengine.graphics.ShapeRenderer;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.TextRenderer;
import de.gurkenlabs.litiengine.graphics.animation.AnimationController;
import de.gurkenlabs.litiengine.gui.GuiComponent;
import de.gurkenlabs.litiengine.gui.ImageComponent;
import de.gurkenlabs.litiengine.util.ImageProcessing;
import de.gurkenlabs.litiengine.util.MathUtilities;

public final class Hud extends GuiComponent {

  private static final Color COLOR_MONEY = new Color(77, 125, 10);
  private static final Color COLOR_MONEY_BORDER = new Color(11, 240, 10, 50);
  private static final Color COLOR_OUTLINE = new Color(0, 0, 0, 120);
  private static final Color COLOR_BG = new Color(0, 0, 0, 150);
  private static final int PADDING = 10;
  private static final BufferedImage MARKER = ImageProcessing.scaleImage(Spritesheet.find("marker").getImage(), 4f);
  private ImageComponent kickButton;
  private AnimationController logoAnimationController;

  public Hud() {
    super(0, 0, Game.getScreenManager().getResolution().getWidth(), Game.getScreenManager().getResolution().getHeight());
  }

  @Override
  public void render(final Graphics2D g) {
    this.renderCurrentFocus(g);
    this.renderClubInfo(g);

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
      e.getEvent().consume();
    });

    hideDismissButton();
    this.logoAnimationController = new AnimationController(Spritesheet.find("Logo_anim"));
    Game.getLoop().attach(this.logoAnimationController);
  }

  @Override
  protected void initializeComponents() {
    super.initializeComponents();

    double width = Game.getScreenManager().getResolution().getWidth() / 10;
    double height = Game.getScreenManager().getResolution().getHeight() / 10;
    this.kickButton = new ImageComponent(Game.getScreenManager().getResolution().getWidth() / 2.0 - width / 2.0, Game.getScreenManager().getResolution().getHeight() / 2 + height, width, height);
    this.kickButton.setText("DISMISS");
    this.kickButton.setFont(Program.GUI_FONT.deriveFont(Font.BOLD));
    this.kickButton.getAppearance().setForeColor(new Color(68, 13, 13, 220));
    this.kickButton.getAppearance().setBackgroundColor1(new Color(255, 0, 0, 150));
    this.kickButton.getAppearance().setTransparentBackground(false);
    this.kickButton.getAppearanceHovered().setForeColor(new Color(68, 13, 13, 255));
    this.kickButton.getAppearanceHovered().setBackgroundColor1(new Color(255, 0, 0, 190));
    this.kickButton.getAppearanceHovered().setTransparentBackground(false);

    this.getComponents().add(this.kickButton);
  }

  private void renderCurrentFocus(Graphics2D g) {
    if (GameManager.getCurrentFocus() == null) {
      return;
    }

    PartyGuest guest = GameManager.getCurrentFocus();

    final Point2D loc = Game.getCamera().getViewPortLocation(guest.getCenter());
    ImageRenderer.render(g, MARKER, (loc.getX() * Game.getRenderEngine().getBaseRenderScale() - MARKER.getWidth() / 2.0), loc.getY() * Game.getRenderEngine().getBaseRenderScale() - (MARKER.getHeight() * 3));

    BufferedImage image = guest.getAnimationController().getCurrentSprite();
    double factor = 150 / image.getWidth();
    double imgWidth = factor * image.getWidth();
    double imgHeight = factor * image.getHeight();
    double imgX = Game.getScreenManager().getResolution().getWidth() / 2.0 - imgWidth / 2.0;
    double imgY = this.kickButton.getY() + this.kickButton.getHeight() + imgHeight * 0.1;
    ImageRenderer.renderScaled(g, guest.getAnimationController().getCurrentSprite(), imgX, imgY, factor);

    // render clubbing since
    g.setFont(Program.GUI_FONT_SMALL);
    Time clubbingSince = new Time(GameManager.getStartTime().getTime() + Game.getLoop().convertToMs(guest.getClubbingSince()) * 60);
    SimpleDateFormat form = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    String time = "Clubbing since " + form.format(clubbingSince);
    double width = g.getFontMetrics().stringWidth(time);
    double y = Game.getScreenManager().getResolution().getHeight() - g.getFontMetrics().getHeight() * 0.5;
    TextRenderer.render(g, time, Game.getScreenManager().getResolution().getWidth() / 2.0 - width / 2, y);

    // render name bg
    g.setFont(Program.GUI_FONT_SMALL.deriveFont(36f));
    double nameWidth = g.getFontMetrics().stringWidth(guest.getName());
    double nameX = Game.getScreenManager().getResolution().getWidth() / 2.0 - nameWidth / 2;
    double nameY = y - g.getFontMetrics().getHeight();

    Rectangle2D nameBg = new Rectangle2D.Double(nameX - PADDING, nameY - g.getFontMetrics().getHeight(), nameWidth + 2 * PADDING, g.getFontMetrics().getHeight() + PADDING);
    g.setColor(COLOR_BG);
    ShapeRenderer.render(g, nameBg);

    // render name
    g.setColor(Color.WHITE);
    TextRenderer.renderWithOutline(g, guest.getName(), nameX, nameY, COLOR_OUTLINE, RenderingHints.VALUE_ANTIALIAS_ON);
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

  private void renderClubInfo(Graphics2D g) {
    final int LOGO_SIZE = 73;
    final String guestsPlace = "# 000";
    final String emptyPlace = "----";
    final String moneyPlace = "$ 00000";
    final String timePlace = "00:00 PM";
    g.setFont(Program.GUI_FONT_SMALL);

    FontMetrics fm = g.getFontMetrics();
    double bgHeight = fm.getHeight() + PADDING * 2;
    double bgY = Game.getScreenManager().getResolution().getHeight() - bgHeight - PADDING;
    double textY = bgY + fm.getHeight() * 1.1;

    // render right bg
    double rightBgWidth = fm.stringWidth(guestsPlace) + fm.stringWidth(emptyPlace) + fm.stringWidth(moneyPlace) + LOGO_SIZE + PADDING * 3;
    double rightBgX = Game.getScreenManager().getResolution().getWidth() - rightBgWidth - PADDING;
    Rectangle2D rightBg = new Rectangle2D.Double(rightBgX, bgY, rightBgWidth, bgHeight);
    g.setColor(COLOR_BG);
    ShapeRenderer.render(g, rightBg);

    // render left bg
    double leftBgWidth = fm.stringWidth(timePlace) + PADDING * 2;
    double leftBgX = PADDING;
    Rectangle2D leftBg = new Rectangle2D.Double(leftBgX, bgY, leftBgWidth, bgHeight);
    g.setColor(COLOR_BG);
    ShapeRenderer.render(g, leftBg);

    g.setColor(Color.WHITE);

    // render number
    String number = "# " + Game.getEnvironment().getByType(PartyGuest.class).size();
    double numberX = rightBgX + PADDING;
    TextRenderer.renderWithOutline(g, number, numberX, textY, COLOR_OUTLINE, RenderingHints.VALUE_ANTIALIAS_ON);

    // render money
    String money = "$ " + GameManager.getCurrentMoney();
    double moneyX = rightBgX + PADDING + fm.stringWidth(guestsPlace) + fm.stringWidth(emptyPlace);
    TextRenderer.renderWithOutline(g, money, moneyX, textY, COLOR_OUTLINE, RenderingHints.VALUE_ANTIALIAS_ON);

    // render logo
    double logoX = moneyX + fm.stringWidth(moneyPlace) + PADDING * 2;
    ImageRenderer.render(g, this.logoAnimationController.getCurrentSprite(), logoX, bgY - 8);

    // render time
    SimpleDateFormat form = new SimpleDateFormat("hh:mm a");
    String time = form.format(GameManager.getCurrentGameTime());
    double timeX = leftBgX + PADDING;
    TextRenderer.renderWithOutline(g, time, timeX, textY, COLOR_OUTLINE, RenderingHints.VALUE_ANTIALIAS_ON);
  }
}
