package de.gurkenlabs.ldjam42.entities;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.util.MathUtilities;

public class Group {
  static final int MAX_GROUP_SIZE = 9;

  private static int currentGroupId;
  private final List<PartyGuest> members;
  private final int[] groupFeatures = new int[2];
  private final int[] groupFeatureIndices = new int[2];
  private final int id;
  
  private double probability;

  public Group() {
    this.members = new CopyOnWriteArrayList<>();
    this.id = ++currentGroupId;
    this.probability = 1;

    this.groupFeatureIndices[0] = MathUtilities.randomInRange(0, 4);
    this.groupFeatureIndices[1] = MathUtilities.randomInRange(0, 4);
    while (this.groupFeatureIndices[0] == this.groupFeatureIndices[1]) {
      this.groupFeatureIndices[1] = MathUtilities.randomInRange(0, 4);
    }

    this.groupFeatures[0] = MathUtilities.randomInRange(0, 4);
    this.groupFeatures[1] = MathUtilities.randomInRange(0, 4);
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

  public int[] getFeatures() {
    return this.groupFeatures;
  }

  public int[] getFeatureIndices() {
    return this.groupFeatureIndices;
  }
  
  

}
