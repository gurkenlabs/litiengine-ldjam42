package de.gurkenlabs.ldjam42.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.gurkenlabs.ldjam42.GameManager;
import de.gurkenlabs.ldjam42.Program;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.graphics.FreeFlightCamera;
import de.gurkenlabs.litiengine.graphics.ImageRenderer;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.TextRenderer;
import de.gurkenlabs.litiengine.graphics.animation.AnimationController;
import de.gurkenlabs.litiengine.gui.ImageComponent;
import de.gurkenlabs.litiengine.gui.screens.Screen;
import de.gurkenlabs.litiengine.sound.Sound;

public class MenuScreen extends Screen {
  private static final BufferedImage gurkenlabs = Resources.getImage("gurkenlabs-neon.png");
  private AnimationController logoAnimationController;

  public static final String NAME = "MENU";
  public static Sound MENU_MUSIC = Sound.get("tanzendiscodisco_muffled.ogg");
  private ImageComponent playButton;
  private boolean locked;

  public MenuScreen() {
    super(NAME);
  }

  @Override
  public void render(final Graphics2D g) {
    if (Game.getEnvironment() != null) {
      Game.getEnvironment().render(g);
    }

    // render game logo
    final int defaultSize = 73;
    final double logoScale = 6;
    final double size = defaultSize * logoScale;
    double x = Game.getScreenManager().getCenter().getX() - size / 2.0;
    double y = Game.getScreenManager().getCenter().getY() - size;
    ImageRenderer.renderScaled(g, this.logoAnimationController.getCurrentSprite(), x, y, logoScale);

    // render gurkenlabs logo
    final double gurkenX = Game.getScreenManager().getResolution().getWidth() - gurkenlabs.getWidth() - 10;
    final double gurkenY = Game.getScreenManager().getResolution().getHeight() - gurkenlabs.getHeight() - 10;
    ImageRenderer.render(g, gurkenlabs, gurkenX, gurkenY);

    // render info
    String info1 = "© 2018 by gurkenlabs";
    g.setFont(Program.GUI_FONT_SMALL);
    g.setColor(Color.WHITE);
    double infoX1 = Game.getScreenManager().getCenter().getX() - g.getFontMetrics().stringWidth(info1) / 2.0;
    double infoY1 = Game.getScreenManager().getResolution().getHeight() - g.getFontMetrics().getHeight() - 10;
    TextRenderer.render(g, info1, infoX1, infoY1);

    String info2 = "a game made for LDJAM42 in 72 hours";
    g.setFont(Program.GUI_FONT_SMALL);
    g.setColor(Color.WHITE);
    double infoX2 = Game.getScreenManager().getCenter().getX() - g.getFontMetrics().stringWidth(info2) / 2.0;
    double infoY2 = infoY1 - g.getFontMetrics().getHeight() - 10;
    TextRenderer.render(g, info2, infoX2, infoY2);
    super.render(g);
  }

  @Override
  public void prepare() {
    super.prepare();

    this.logoAnimationController = new AnimationController(Spritesheet.find("Logo_anim"));
    Game.getSoundEngine().playMusic(MENU_MUSIC);
    Game.getLoop().attach(this.logoAnimationController);
  }

  @Override
  protected void initializeComponents() {
    super.initializeComponents();
    double x = Game.getScreenManager().getCenter().getX();
    double y = Game.getScreenManager().getCenter().getY();
    double width = Game.getScreenManager().getResolution().getWidth() / 3;
    double height = Game.getScreenManager().getResolution().getHeight() / 6;
    this.playButton = new ImageComponent(x - width / 2.0, y + height / 2.0, width, height);
    this.playButton.setImage(null);
    this.playButton.setText("PLAY GAME");
    this.playButton.setFont(Program.GUI_FONT.deriveFont(100f));
    this.playButton.getAppearance().setForeColor(new Color(215, 82, 82));
    this.playButton.getAppearanceHovered().setForeColor(new Color(253, 184, 184));

    this.playButton.onClicked(e -> {
      if (this.locked) {
        return;
      }

      this.locked = true;
      this.playButton.setEnabled(false);
      Game.getScreenManager().getRenderComponent().fadeOut(1000);
      Game.getLoop().execute(1500, () -> {
        displayIngameScreen();
        Game.getScreenManager().getRenderComponent().fadeIn(1000);
        this.locked = false;
        this.playButton.setEnabled(true);
      });
    });

    this.getComponents().add(this.playButton);
  }

  private static void displayIngameScreen() {
    Game.loadEnvironment(GameManager.getGoin());
    Game.getScreenManager().displayScreen(IngameScreen.NAME);
    FreeFlightCamera camera = new FreeFlightCamera(Game.getEnvironment().getCenter());
    Game.setCamera(camera);
    GameManager.start();
  }
}
