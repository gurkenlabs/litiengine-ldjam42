package de.gurkenlabs.ldjam42.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import de.gurkenlabs.ldjam42.GameManager;
import de.gurkenlabs.ldjam42.constants.GoInSprites;
import de.gurkenlabs.ldjam42.util.IntPermutator;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.FreeFlightCamera;
import de.gurkenlabs.litiengine.gui.ImageComponent;
import de.gurkenlabs.litiengine.gui.screens.Screen;
import de.gurkenlabs.litiengine.util.ImageProcessing;

public class MenuScreen extends Screen {
  public static final String NAME = "MENU";
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

    super.render(g);

  }

  @Override
  protected void initializeComponents() {
    super.initializeComponents();
    double x = Game.getScreenManager().getCenter().getX();
    double y = Game.getScreenManager().getCenter().getX();
    double width = Game.getScreenManager().getResolution().getWidth() / 5;
    double height = Game.getScreenManager().getResolution().getHeight() / 7;
    this.playButton = new ImageComponent(x - width / 2.0, y - height / 2.0, width, height);
    this.playButton.setImage(null);
    this.playButton.setText("PLAY GAME");

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
