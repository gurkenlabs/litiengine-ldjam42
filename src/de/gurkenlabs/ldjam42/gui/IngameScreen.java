package de.gurkenlabs.ldjam42.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import de.gurkenlabs.ldjam42.GameManager;
import de.gurkenlabs.ldjam42.GameState;
import de.gurkenlabs.ldjam42.Program;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.graphics.TextRenderer;
import de.gurkenlabs.litiengine.gui.ImageComponent;
import de.gurkenlabs.litiengine.gui.Menu;
import de.gurkenlabs.litiengine.gui.screens.Screen;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.sound.Sound;
import de.gurkenlabs.litiengine.util.MathUtilities;

public class IngameScreen extends Screen {
  public static final int TUTORIAL_DURATION = 10000;
  private static final Color COLOR_MONEY = new Color(77, 125, 10);
  private static final Color COLOR_MONEY_BORDER = new Color(11, 240, 10, 50);

  public static final String NAME = "INGAME";
  private static final BufferedImage pauseOverlay = Resources.getImage("pause-overlay.png");
  private static IngameScreen instance;
  private ImageComponent restartButton;

  public static Sound INGAME_MUSIC = Sound.get("ingame-music.ogg");

  private Hud hud;
  private Menu ingameMenu;
  private long tutorialTick;

  private IngameScreen() {
    super(NAME);
  }

  public static IngameScreen instance() {
    if (instance == null) {
      instance = new IngameScreen();
    }

    return instance;
  }

  @Override
  public void render(final Graphics2D g) {
    if (Game.getEnvironment() != null) {
      Game.getEnvironment().render(g);
    }

    this.restartButton.setVisible(GameManager.getGameState() == GameState.ENDSCREEN);
    if (GameManager.getGameState() == GameState.ENDSCREEN) {
      this.hideIngameMenu();
      g.drawImage(pauseOverlay, 0, 0, (int) Game.getScreenManager().getResolution().getWidth(),
          (int) Game.getScreenManager().getResolution().getHeight(), null);

      MenuScreen.renderGameLogo(g);

      g.setColor(new Color(253, 184, 184));
      g.setFont(Program.GUI_FONT);
      String phrase = "The night is over but the party never stops!";
      double x = Game.getScreenManager().getCenter().getX() - g.getFontMetrics().stringWidth(phrase) / 2.0;
      double y = Game.getScreenManager().getCenter().getY();
      TextRenderer.renderWithOutline(g, phrase, x, y, Hud.COLOR_OUTLINE, RenderingHints.VALUE_ANTIALIAS_ON);

      this.renderMoney(g);

      g.setColor(new Color(182, 225, 253));
      String kicked = GameManager.getKickedGuests().size() + " party people were sent home!";
      double kickedX = Game.getScreenManager().getCenter().getX() - g.getFontMetrics().stringWidth(kicked) / 2.0;
      double kickedY = y + 280;
      TextRenderer.renderWithOutline(g, kicked, kickedX, kickedY, Hud.COLOR_OUTLINE, RenderingHints.VALUE_ANTIALIAS_ON);

      this.restartButton.render(g);
      return;
    }

    if (GameManager.getGameState() == GameState.PAUSED) {
      g.drawImage(pauseOverlay, 0, 0, (int) Game.getScreenManager().getResolution().getWidth(),
          (int) Game.getScreenManager().getResolution().getHeight(), null);
    }

    this.renderTutorial(g);

    super.render(g);
  }

  private void renderTutorial(Graphics2D g) {
    // render status
    long deltaTime = Game.getLoop().getDeltaTime(this.tutorialTick);
    if (this.tutorialTick != 0 && deltaTime <= TUTORIAL_DURATION) {
      g.setColor(Color.WHITE);
      // fade out status color
      final double fadeOutTime = 0.75 * TUTORIAL_DURATION;
      if (deltaTime > fadeOutTime) {
        double fade = deltaTime - fadeOutTime;
        int alpha = (int) (255 - (fade / (TUTORIAL_DURATION - fadeOutTime)) * 255);
        g.setColor(new Color(255, 255, 255, MathUtilities.clamp(alpha, 0, 255)));
      }

      Font old = g.getFont();
      g.setFont(Program.GUI_FONT);
      FontMetrics fm = g.getFontMetrics();
      final String tut1 = "Give your guests the space they need.";
      TextRenderer.renderWithOutline(g, tut1, Game.getScreenManager().getResolution().getWidth() / 2d - fm.stringWidth(tut1) / 2.0, Game.getScreenManager().getCenter().getY(), Hud.COLOR_OUTLINE, RenderingHints.VALUE_ANTIALIAS_ON);
      final String tut2 = "You earn more $$$ from satisfied customers! Keep them happy and ...";
      TextRenderer.renderWithOutline(g, tut2, Game.getScreenManager().getResolution().getWidth() / 2d - fm.stringWidth(tut2) / 2.0, Game.getScreenManager().getCenter().getY() + fm.getHeight(), Hud.COLOR_OUTLINE, RenderingHints.VALUE_ANTIALIAS_ON);

      g.setFont(Program.GUI_FONT.deriveFont(80f));
      final String tut3 = "PARTY HARD!";
      TextRenderer.renderWithOutline(g, tut3, Game.getScreenManager().getResolution().getWidth() / 2d - g.getFontMetrics().stringWidth(tut3) / 2.0, Game.getScreenManager().getCenter().getY() + g.getFontMetrics().getHeight() * 2, Hud.COLOR_OUTLINE, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setFont(old);
    }
  }

  private void renderMoney(Graphics2D g) {
    g.setColor(COLOR_MONEY);

    g.setFont(Program.GUI_FONT.deriveFont(80f));
    String unit = "$";
    double unitWidth = g.getFontMetrics().stringWidth(unit);
    double unitHeight = g.getFontMetrics().getHeight();
    double unitY = Game.getScreenManager().getCenter().getY() + unitHeight * 1.1;
    g.setFont(Program.GUI_FONT.deriveFont(80f));
    TextRenderer.renderWithOutline(g, unit, Game.getScreenManager().getResolution().getWidth() / 2.0 - unitWidth / 2, unitY, COLOR_MONEY_BORDER, RenderingHints.VALUE_ANTIALIAS_ON);

    g.setFont(Program.GUI_FONT.deriveFont(70f));
    String money = GameManager.getCurrentMoney() + "";
    double width = g.getFontMetrics().stringWidth(money);
    double height = g.getFontMetrics().getHeight();
    double moneyY = height + unitY;
    TextRenderer.renderWithOutline(g, money, Game.getScreenManager().getResolution().getWidth() / 2.0 - width / 2, moneyY, COLOR_MONEY_BORDER, RenderingHints.VALUE_ANTIALIAS_ON);
  }

  public Hud getHud() {
    return this.hud;
  }

  @Override
  public void prepare() {
    super.prepare();
    GameManager.setGameState(GameState.INGAME);
    this.ingameMenu.setVisible(false);
    this.restartButton.setVisible(false);

    Input.keyboard().onKeyTyped(KeyEvent.VK_ESCAPE, e -> {
      if (GameManager.getGameState() == GameState.ENDSCREEN) {
        System.exit(0);
        return;
      }

      toggleIngameMenu();
    });

    Game.getLoop().setTimeScale(1);
    Game.getSoundEngine().playMusic(INGAME_MUSIC);
    this.tutorialTick = Game.getLoop().getTicks();
  }

  private void hideIngameMenu() {
    this.ingameMenu.setVisible(false);
    Game.getLoop().setTimeScale(1);
  }

  private void toggleIngameMenu() {
    this.ingameMenu.setVisible(!this.ingameMenu.isVisible());

    GameManager.setGameState(this.ingameMenu.isVisible() ? GameState.PAUSED : GameState.INGAME);

    if (GameManager.getGameState() == GameState.PAUSED) {
      Game.getLoop().setTimeScale(0);
    } else {
      Game.getLoop().setTimeScale(1);
    }
  }

  @Override
  protected void initializeComponents() {
    this.hud = new Hud();
    this.getComponents().add(this.hud);

    double width = Game.getScreenManager().getResolution().getWidth() / 4;
    double height = Game.getScreenManager().getResolution().getHeight() / 6;
    double x = Game.getScreenManager().getCenter().getX() - width / 2.0;
    double y = Game.getScreenManager().getCenter().getY() - height / 2.0;
    this.ingameMenu = new Menu(x, y, width, height, "Restart Party", "End Night");
    this.ingameMenu.onChange(i -> {
      if (i == 0) {
        GameManager.restart();
        this.toggleIngameMenu();
        return;
      }

      if (i == 1) {
        System.exit(0);
      }
    });

    this.getComponents().add(this.ingameMenu);

    double w = Game.getScreenManager().getResolution().getWidth() / 3;
    double h = Game.getScreenManager().getResolution().getHeight() / 6;
    double restartX = Game.getScreenManager().getCenter().getX() - w / 2.0;
    double restartY = Game.getScreenManager().getResolution().getHeight() - h - 10;
    this.restartButton = new ImageComponent(restartX, restartY, w, h);
    this.restartButton.setImage(null);
    this.restartButton.setText("PARTY ON");
    this.restartButton.setFont(Program.GUI_FONT.deriveFont(100f));
    this.restartButton.getAppearance().setForeColor(new Color(215, 82, 82));
    this.restartButton.getAppearanceHovered().setForeColor(new Color(253, 184, 184));

    this.restartButton.onClicked(e -> {
      GameManager.restart();
      GameManager.setGameState(GameState.INGAME);
    });

    this.getComponents().add(this.restartButton);
  }
}
