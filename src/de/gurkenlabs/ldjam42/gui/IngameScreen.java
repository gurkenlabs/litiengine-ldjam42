package de.gurkenlabs.ldjam42.gui;

import java.awt.Graphics2D;

import de.gurkenlabs.ldjam42.GameManager;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.gui.screens.Screen;
import de.gurkenlabs.litiengine.sound.Sound;

public class IngameScreen extends Screen {
  public static final String NAME = "INGAME";
  public static Sound INGAME_MUSIC = Sound.get("ingame-music.ogg");

  private Hud hud;

  private static IngameScreen instance;

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

    super.render(g);
  }

  public Hud getHud() {
    return this.hud;
  }

  @Override
  public void prepare() {
    super.prepare();
    Game.getSoundEngine().playMusic(INGAME_MUSIC);
  }

  @Override
  protected void initializeComponents() {
    this.hud = new Hud();
    this.getComponents().add(this.hud);
  }
}
