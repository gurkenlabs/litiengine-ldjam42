package de.gurkenlabs.ldjam42.gui;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import de.gurkenlabs.ldjam42.GameManager;
import de.gurkenlabs.ldjam42.GameState;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.gui.Menu;
import de.gurkenlabs.litiengine.gui.screens.Screen;
import de.gurkenlabs.litiengine.input.Input;

public class IngameScreen extends Screen {
  public static final String NAME = "INGAME";
  private static final BufferedImage pauseOverlay = Resources.getImage("pause-overlay.png");
  private static IngameScreen instance;

  private Hud hud;
  private Menu ingameMenu;

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

    if (GameManager.getGrid() != null) {
      // GameManager.getGrid().render(g);
    }

    if (GameManager.getGameState() == GameState.PAUSED) {
      g.drawImage(pauseOverlay, 0, 0, (int) Game.getScreenManager().getResolution().getWidth(),
          (int) Game.getScreenManager().getResolution().getHeight(), null);
    }

    super.render(g);
  }

  public Hud getHud() {
    return this.hud;
  }

  @Override
  public void prepare() {
    super.prepare();
    GameManager.setGameState(GameState.INGAME);
    this.ingameMenu.setVisible(false);

    Input.keyboard().onKeyTyped(KeyEvent.VK_ESCAPE, e -> {
      toggleIngameMenu();
    });
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
        return;
      }

      if (i == 1) {
        System.exit(0);
      }
    });

    this.getComponents().add(this.ingameMenu);
  }
}
