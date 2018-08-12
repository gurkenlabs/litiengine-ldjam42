package de.gurkenlabs.ldjam42.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import de.gurkenlabs.ldjam42.GameManager;
import de.gurkenlabs.ldjam42.constants.GoInSprites;
import de.gurkenlabs.ldjam42.util.SpritePermutator;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.FreeFlightCamera;
import de.gurkenlabs.litiengine.gui.ImageComponent;
import de.gurkenlabs.litiengine.gui.screens.Screen;
import de.gurkenlabs.litiengine.util.ImageProcessing;

public class MenuScreen extends Screen {
  public static final String NAME = "MENU";
  private ImageComponent playButton;
  private SpritePermutator perm;
  private Image[][] features;
  private boolean locked;

  public MenuScreen() {
    super(NAME);
  }

  @Override
  public void render(final Graphics2D g) {
    if (Game.getEnvironment() != null) {
      Game.getEnvironment().render(g);
    }
    g.drawImage(ImageProcessing.scaleImage((BufferedImage) this.perm.getSpritePermutationsInOneImage(), 3.0f), 200, 0, null);
    int testX = 500;
    int testY = 50;
    float testScale = 3.0f;
    for (int typeIndex = 0; typeIndex < this.features.length; typeIndex++) {
      for (int featureIndex = 0; featureIndex < this.features[typeIndex].length; featureIndex++) {
        BufferedImage img = (BufferedImage) this.features[typeIndex][featureIndex];
        int x = (int) (testX + featureIndex * img.getWidth() * testScale);
        int y = (int) (testY + typeIndex * img.getHeight() * testScale);
        g.drawImage(ImageProcessing.scaleImage(img, testScale), x, y, null);
        g.setColor(Color.WHITE);
        g.drawRect(x, y, (int) (img.getWidth() * testScale), (int) (img.getHeight() * testScale));
        g.drawString(typeIndex + " - " + featureIndex, x + 20, y + img.getHeight() * testScale);
      }
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

    this.features = new Image[][] { { GoInSprites.IDLE_DOWN_PANTS0.getImage(), GoInSprites.IDLE_DOWN_PANTS1.getImage(), GoInSprites.IDLE_DOWN_PANTS2.getImage(), GoInSprites.IDLE_DOWN_PANTS3.getImage() },
        { GoInSprites.IDLE_DOWN_TOP0.getImage(), GoInSprites.IDLE_DOWN_TOP1.getImage(), GoInSprites.IDLE_DOWN_TOP2.getImage(), GoInSprites.IDLE_DOWN_TOP3.getImage() },
        { GoInSprites.IDLE_DOWN_FACE0.getImage(), GoInSprites.IDLE_DOWN_FACE1.getImage(), GoInSprites.IDLE_DOWN_FACE2.getImage(), GoInSprites.IDLE_DOWN_FACE3.getImage() },
        { GoInSprites.IDLE_DOWN_HAIR0.getImage(), GoInSprites.IDLE_DOWN_HAIR1.getImage(), GoInSprites.IDLE_DOWN_HAIR2.getImage(), GoInSprites.IDLE_DOWN_HAIR3.getImage() } };
    this.perm = new SpritePermutator(features);
  }

  private static void displayIngameScreen() {
    Game.loadEnvironment(GameManager.getGoin());
    Game.getScreenManager().displayScreen(IngameScreen.NAME);
    FreeFlightCamera camera = new FreeFlightCamera(Game.getEnvironment().getCenter());
    Game.setCamera(camera);
    GameManager.start();
  }
}
