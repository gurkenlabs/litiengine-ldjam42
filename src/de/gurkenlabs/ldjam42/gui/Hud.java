package de.gurkenlabs.ldjam42.gui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.swing.JPanel;

import de.gurkenlabs.ldjam42.ClubArea;
import de.gurkenlabs.ldjam42.GameManager;
import de.gurkenlabs.ldjam42.Program;
import de.gurkenlabs.ldjam42.entities.PartyGuest;
import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.graphics.DebugRenderer;
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
  private static final Color COLOR_OUTLINE = new Color(0, 0, 0, 180);
  private static final Color COLOR_BG = new Color(0, 0, 0, 150);
  private static final Color COLOR_BAD = new Color(238, 28, 37, 220);
  private static final Color COLOR_AVG = new Color(238, 160, 36, 220);
  private static final Color COLOR_GOOD = new Color(143, 212, 61, 220);
  private static final int PADDING = 10;
  private static final BufferedImage MARKER = ImageProcessing.scaleImage(Spritesheet.find("marker").getImage(), 4f);
  private static final BufferedImage SMILEY_BAD = Spritesheet.find("smileys").getSprite(0);
  private static final BufferedImage SMILEY_AVG = Spritesheet.find("smileys").getSprite(1);
  private static final BufferedImage SMILEY_GOOD = Spritesheet.find("smileys").getSprite(2);

  private ImageComponent kickButton;
  private AnimationController logoAnimationController;

  public Hud() {
    super(0, 0, Game.getScreenManager().getResolution().getWidth(), Game.getScreenManager().getResolution().getHeight());

    Game.getRenderEngine().onEntityRendered(e -> {
      DebugRenderer.renderEntityDebugInfo(e.getGraphics(), e.getRenderedObject());

      if (Game.getScreenManager().getCurrentScreen() instanceof IngameScreen && e.getRenderedObject() instanceof PartyGuest) {
        this.renderSatisfaction(e.getGraphics(), (PartyGuest) e.getRenderedObject());
      }
    });
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
    final BufferedImage icon = Spritesheet.find("dismiss").getImage();
    final int iconSize = icon.getWidth();
    double size = iconSize + PADDING * 2;
    double x = Game.getScreenManager().getResolution().getWidth() / 2.0 - size / 2.0;
    double y = Game.getScreenManager().getResolution().getHeight() / 2 + size / 4;
    this.kickButton = new ImageComponent(x, y, size, size, icon);
    this.kickButton.setImageValign(Valign.MIDDLE);
    this.kickButton.setImageAlign(Align.CENTER);
    this.kickButton.getAppearance().setBackgroundColor1(new Color(255, 0, 0, 150));
    this.kickButton.getAppearance().setTransparentBackground(false);
    this.kickButton.getAppearanceHovered().setBackgroundColor1(new Color(255, 0, 0, 190));
    this.kickButton.getAppearanceHovered().setTransparentBackground(false);

    this.getComponents().add(this.kickButton);
  }

  private void renderCurrentFocus(Graphics2D g) {
    if (GameManager.getCurrentFocus() == null) {
      return;
    }

    PartyGuest guest = GameManager.getCurrentFocus();

    // render marker
    final Point2D loc = Game.getCamera().getViewPortLocation(guest.getCenter());
    ImageRenderer.render(g, MARKER, (loc.getX() * Game.getRenderEngine().getBaseRenderScale() - MARKER.getWidth() / 2.0), loc.getY() * Game.getRenderEngine().getBaseRenderScale() - (MARKER.getHeight() * 3) -5);

    // render clubbing bg
    g.setFont(Program.GUI_FONT_SMALL);
    FontMetrics fm = g.getFontMetrics();
    double clubbingBgHeight = fm.getHeight() + PADDING * 2;
    double clubbingBgY = Game.getScreenManager().getResolution().getHeight() - clubbingBgHeight - PADDING;
    double clubbingY = clubbingBgY + fm.getHeight() * 1.1;
    double clubbingBgWidth = fm.stringWidth("Clubbing since 00:00 AM") + PADDING * 2;
    double clubbingBgX = Game.getScreenManager().getResolution().getWidth() / 2.0 - clubbingBgWidth / 2.0;
    Rectangle2D clubbinBg = new Rectangle2D.Double(clubbingBgX, clubbingBgY, clubbingBgWidth, clubbingBgHeight);
    g.setColor(COLOR_BG);
    ShapeRenderer.render(g, clubbinBg);

    // render clubbing since
    Time clubbingSince = new Time(GameManager.getStartTime().getTime() + Game.getLoop().convertToMs(guest.getClubbingSince()) * 60);
    SimpleDateFormat form = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    String time = "Clubbing since " + form.format(clubbingSince);
    double clubbingWidth = g.getFontMetrics().stringWidth(time);
    g.setColor(Color.WHITE);
    TextRenderer.renderWithOutline(g, time, Game.getScreenManager().getResolution().getWidth() / 2.0 - clubbingWidth / 2, clubbingY, COLOR_OUTLINE, RenderingHints.VALUE_ANTIALIAS_ON);

    // render satisfaction bar 
    final double satisfactionBarHeight = 20;
    double barWidth = guest.getSatisfaction() * clubbingBgWidth;
    double barBgX = Game.getScreenManager().getResolution().getWidth() / 2.0 - barWidth / 2.0;
    double barY = clubbingBgY - PADDING - satisfactionBarHeight;
    Rectangle2D satisfaction = new Rectangle2D.Double(barBgX, barY, barWidth, satisfactionBarHeight);
    Color color = COLOR_BAD;
    if (guest.getSatisfaction() > 0.25) {
      color = COLOR_AVG;
    }

    if (guest.getSatisfaction() > 0.7) {
      color = COLOR_GOOD;
    }

    g.setColor(color);
    ShapeRenderer.render(g, satisfaction);

    g.setFont(new JPanel().getFont().deriveFont(16f));
    g.setColor(Color.WHITE);
    String satText = (int) (guest.getSatisfaction() * 100) + "%";
    double satTextX = Game.getScreenManager().getResolution().getWidth() / 2.0 - g.getFontMetrics().stringWidth(satText) / 2.0;
    double satTextY = barY + g.getFontMetrics().getHeight() * 0.75;
    TextRenderer.renderWithOutline(g, satText, satTextX, satTextY, COLOR_OUTLINE, RenderingHints.VALUE_ANTIALIAS_ON);

    // render name bg
    g.setFont(Program.GUI_FONT_SMALL.deriveFont(36f));
    fm = g.getFontMetrics();
    double nameBgHeight = fm.getHeight() + PADDING * 2;
    double nameBgY = clubbingBgY - clubbingBgHeight - satisfactionBarHeight - PADDING * 3;
    double nameBgWidth = fm.stringWidth(guest.getName()) + PADDING * 2;
    double nameBgX = Game.getScreenManager().getResolution().getWidth() / 2.0 - nameBgWidth / 2;
    Rectangle2D nameBg = new Rectangle2D.Double(nameBgX, nameBgY, nameBgWidth, nameBgHeight);
    g.setColor(COLOR_BG);
    ShapeRenderer.render(g, nameBg);

    // render name
    double nameY = nameBgY + fm.getHeight() * 1.1;
    g.setColor(Color.WHITE);
    TextRenderer.renderWithOutline(g, guest.getName(), nameBgX + PADDING, nameY, COLOR_OUTLINE, RenderingHints.VALUE_ANTIALIAS_ON);

    // render guest image
    BufferedImage image = guest.getAnimationController().getCurrentSprite();
    double factor = 150 / image.getWidth();
    double imgWidth = factor * image.getWidth();
    double imgHeight = factor * image.getHeight();
    double imgX = Game.getScreenManager().getResolution().getWidth() / 2.0 - imgWidth / 2.0;
    double imgY = nameBgY - imgHeight - PADDING;
    ImageRenderer.renderScaled(g, guest.getAnimationController().getCurrentSprite(), imgX, imgY, factor);
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

  private void renderSatisfaction(final Graphics2D g, final PartyGuest guest) {
    final Point2D location = new Point2D.Double(
        Game.getCamera().getViewPortDimensionCenter(guest).getX() - SMILEY_AVG.getWidth() * 0.25 / 2.0 - 4,
        Game.getCamera().getViewPortDimensionCenter(guest).getY() - guest.getHeight() * 3 / 4);

    if (guest.getSatisfaction() > .7) {
      ImageRenderer.renderScaled(g, SMILEY_GOOD, location, 0.25);
      return;
    }

    if (guest.getSatisfaction() > .25) {
      ImageRenderer.renderScaled(g, SMILEY_AVG, location, 0.25);
      return;
    }

    ImageRenderer.renderScaled(g, SMILEY_BAD, location, 0.25);
  }
}
