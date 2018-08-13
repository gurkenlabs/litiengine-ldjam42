package de.gurkenlabs.ldjam42.entities;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.util.ArrayUtilities;

public class Group {
  static final int MAX_GROUP_SIZE = 9;

  private static int currentGroupId;
  private final List<PartyGuest> members;
  private final int[] groupFeatureValues = new int[1];
  private final Feature[] groupFeatureIndices = new Feature[1];
  private final int id;

  private double probability;

  public Group() {
    this.members = new CopyOnWriteArrayList<>();
    this.id = ++currentGroupId;
    this.probability = 1;

    this.groupFeatureIndices[0] = ArrayUtilities.getRandom(Feature.values());
    this.groupFeatureValues[0] = this.groupFeatureIndices[0].getRandomValue();
  }

  public double getProbability() {
    return this.probability;
  }

  public void setProbability(double probability) {
    this.probability = probability;
  }

  public void add(PartyGuest guest) {
    this.getMembers().add(guest);
  }

  public List<PartyGuest> getMembers() {
    return this.members;
  }

  public int getSize() {
    return this.getMembers().size();
  }

  public int getId() {
    return this.id;
  }

  public int[] getFeatureValues() {
    return this.groupFeatureValues;
  }

  public Feature[] getFeatures() {
    return this.groupFeatureIndices;
  }

  public int getFeature(Feature feature) {
    for (int i = 0; i < this.groupFeatureIndices.length; i++) {
      if (this.groupFeatureIndices[i] == feature) {
        return this.groupFeatureValues[i];
      }
    }
    
    return -1;
  }
}
