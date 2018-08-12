package de.gurkenlabs.ldjam42.gui;

import java.awt.Graphics2D;
import java.util.Collection;
import java.util.Optional;

import javax.swing.SwingUtilities;

import de.gurkenlabs.ldjam42.GameManager;
import de.gurkenlabs.ldjam42.entities.PartyGuest;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.gui.screens.Screen;
import de.gurkenlabs.litiengine.input.Input;

public class IngameScreen extends Screen {
  public static final String NAME = "INGAME";
  private Hud hud;

  public IngameScreen() {
    super(NAME);
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

  @Override
  public void prepare() {
    super.prepare();

    Input.mouse().onPressed(e -> {
      if (SwingUtilities.isLeftMouseButton(e)) {
        GameManager.setCurrentFocus(getFocusedGuest());
      }
    });
  }

  @Override
  protected void initializeComponents() {
    this.hud = new Hud();
    this.getComponents().add(this.hud);
  }

  private static PartyGuest getFocusedGuest() {
    Collection<PartyGuest> guests = Game.getEnvironment().getByType(PartyGuest.class);
    Optional<PartyGuest> guest = guests.stream().filter(x -> x.getHitBox().contains(Input.mouse().getMapLocation())).findFirst();
    if (guest.isPresent()) {
      return guest.get();
    }

    return null;
  }
}
