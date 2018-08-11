package de.gurkenlabs.ldjam42.entities;

import java.util.List;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Spawnpoint;
import de.gurkenlabs.litiengine.environment.EntitySpawner;

public class PartyGuestSpawner extends EntitySpawner<PartyGuest> {
  public PartyGuestSpawner(List<Spawnpoint> spawnpoints, int interval, int amount) {
    super(Game.getEnvironment(), Game.getLoop(), spawnpoints, interval, amount);
    this.setSpawnMode(SpawnMode.RANDOMSPAWNPOINTS);
  }

  @Override
  public PartyGuest createNew() {
    System.out.println("new party guest spawned");
    return new PartyGuest(this.getSpawnPoints().get(0).getLocation());
  }
}
