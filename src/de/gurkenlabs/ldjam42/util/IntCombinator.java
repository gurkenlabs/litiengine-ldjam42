package de.gurkenlabs.ldjam42.util;

import java.util.ArrayList;
import java.util.List;

/**
 * This class generates all permutations of numbers from 0 to the maximum given
 * int.
 *
 */
public class IntCombinator {
  private List<int[]> permutations;
  private int max;

  /**
   * @param features the max number
   *
   */
  public IntCombinator(int features) {
    this.permutations = new ArrayList<>();
    this.max = features;
    this.generateIndexCombinations();
  }

  private void generateIndexCombinations() {
    int[] possibleIndices = new int[this.max];
    for (int i = 0; i < this.max; i++) {
      possibleIndices[i] = i;
    }
    this.combineFeatures(possibleIndices);
// debug print
//    System.out.println("Total permutations: " + this.permutations.size());
//    for (int[] p : this.permutations) {
//      String s = "";
//      for (int pi : p) {
//        s += pi + " ";
//      }
//      System.out.println(s);
//    }
  }

  private void combineFeatures(int[] numbers) {
    for (int i1 : numbers) {
      for (int i2 : numbers) {
        for (int i3 : numbers) {
          for (int i4 : numbers) {
            int[] combination = new int[] { i1, i2, i3, i4 };
            this.permutations.add(combination);
          }
        }
      }
    }
  }

  public List<int[]> getPermutations() {
    return this.permutations;
  }
}