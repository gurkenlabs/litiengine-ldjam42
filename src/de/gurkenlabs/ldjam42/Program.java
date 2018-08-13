package de.gurkenlabs.ldjam42;

import java.awt.Font;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.util.Locale;

import de.gurkenlabs.ldjam42.entities.ClubLightsourceMapObjectLoader;
import de.gurkenlabs.ldjam42.entities.Speaker;
import de.gurkenlabs.ldjam42.gui.IngameScreen;
import de.gurkenlabs.ldjam42.gui.MenuScreen;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.PropMapObjectLoader;
import de.gurkenlabs.litiengine.gui.GuiProperties;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.util.ImageProcessing;

public class Program {
  public static final Font GUI_FONT = Resources.getFont("marquee moon.ttf").deriveFont(48f);
  public static final Font GUI_FONT_SMALL = GUI_FONT.deriveFont(30f);
  public static float HUD_SCALE = 2.0f;

  public static final Image CURSOR_STANDARD = ImageProcessing.scaleImage(Resources.getImage("cursor-standard.png"), HUD_SCALE);
  public static final Image CURSOR_CLICK = ImageProcessing.scaleImage(Resources.getImage("cursor-click.png"), HUD_SCALE);

  /**
   * The main entry point for the GOIN club.
   * 
   * @param args
   */
  public static void main(String[] args) {
    Locale.setDefault(new Locale("en", "US"));
    Game.getInfo().setVersion("v0.0.1");
    Game.getInfo().setName("GO IN");
    Game.getInfo().setSubTitle("Behave or GET LOST!");
    Game.init(args);
    initGoin();
    Game.getScreenManager().getRenderComponent().setCursor(CURSOR_STANDARD);
    Game.getScreenManager().getRenderComponent().setCursorOffset(0, 0);

    Game.start();
  }

  private static void initGoin() {
    // load all game assets
    Game.load("game.litidata");

    // init default UI settings
    GuiProperties.setDefaultFont(GUI_FONT);
    GuiProperties.getDefaultAppearance().setTextAntialiasing(RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    GuiProperties.getDefaultAppearanceDisabled().setTextAntialiasing(RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    GuiProperties.getDefaultAppearanceHovered().setTextAntialiasing(RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    Game.getScreenManager().addScreen(new MenuScreen());
    Game.getScreenManager().addScreen(IngameScreen.instance());

    Input.keyboard().onKeyPressed(KeyEvent.VK_ESCAPE, e -> {
      // TODO: implement confirmation; for now, we use alt+f4
      // System.exit(0);
    });

    Game.getRenderEngine().setBaseRenderScale(4);

    Environment.registerMapObjectLoader(new ClubLightsourceMapObjectLoader());
    PropMapObjectLoader.registerCustomPropType(Speaker.class);

    GameManager.init();
  }
}
