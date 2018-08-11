package de.gurkenlabs.ldjam42;

import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;

import de.gurkenlabs.ldjam42.gui.IngameScreen;
import de.gurkenlabs.ldjam42.gui.MenuScreen;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.gui.GuiProperties;
import de.gurkenlabs.litiengine.input.Input;

public class Program {
  public static final Font GUI_FONT = Resources.getFont("marquee moon.ttf").deriveFont(48f);

  /**
   * The main entry point for the GOIN club.
   * 
   * @param args
   */
  public static void main(String[] args) {
    Game.getInfo().setVersion("v0.0.1");
    Game.getInfo().setName("GO IN");
    Game.getInfo().setSubTitle("Behave or GET LOST!");

    Game.init(args);
    initGoin();

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
    Game.getScreenManager().addScreen(new IngameScreen());

    Input.keyboard().onKeyPressed(KeyEvent.VK_ESCAPE, e -> {
      // TODO: implement confirmation; for now, we use alt+f4
      // System.exit(0);
    });

    Game.getRenderEngine().setBaseRenderScale(4);
    GameManager.init();
  }
}
