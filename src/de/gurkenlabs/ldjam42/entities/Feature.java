package de.gurkenlabs.ldjam42.entities;

import de.gurkenlabs.litiengine.util.MathUtilities;

public enum Feature {
  PANTS,
  TOP,
  FACE,
  HAIR;

  public static final int NAKED_VALUE_INDEX = 0;
  public static final int FEATURE_VALUES = 4;
  private static final double NAKED_VALUE_PROB = 0.05;

  public int getRandomValue() {
    // special cases for naked features
    if (this == PANTS || this == Feature.TOP) {
      double[] probabilities = new double[FEATURE_VALUES];

      double share = 1.0 / FEATURE_VALUES;

      double incShare = share + (share - NAKED_VALUE_PROB) / (FEATURE_VALUES - 1);
      for (int i = 0; i < probabilities.length; i++) {
        probabilities[i] = incShare;
      }

      probabilities[NAKED_VALUE_INDEX] = NAKED_VALUE_PROB;
      return MathUtilities.getRandomIndex(probabilities);
    }

    return MathUtilities.randomInRange(0, FEATURE_VALUES);
  }
}
