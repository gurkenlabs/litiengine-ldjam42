package de.gurkenlabs.ldjam42.util;

import java.util.ArrayList;
import java.util.List;

/**
 * This class generates all permutations of numbers from 0 to the maximum given
 * int.
 *
 */
public class IntPermutator {
  private List<int[]> permutations;
  private int max;

  /**
   * @param features the max number
   *
   */
  public IntPermutator(int features) {
    this.permutations = new ArrayList<>();
    this.max = features;
    this.generateIndexPermutations();
  }

  private void generateIndexPermutations() {
    int[] possibleIndices = new int[this.max];
    for (int i = 0; i < this.max; i++) {
      possibleIndices[i] = i;
    }
    this.heapPermutation(possibleIndices, this.max, this.max);
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

//Generating permutation using Heap Algorithm
  private void heapPermutation(int[] a, int size, int n) {
    // if size becomes 1 then add the obtained permutation
    if (size == 1) {
      int[] permutation = new int[n];
      for (int i = 0; i < n; i++) {
        permutation[i] = a[i];
      }
      this.permutations.add(permutation);
    }

    for (int i = 0; i < size; i++) {
      heapPermutation(a, size - 1, n);

      // if size is odd, swap first and last
      // element
      if (size % 2 == 1) {
        int temp = a[0];
        a[0] = a[size - 1];
        a[size - 1] = temp;
      }

      // If size is even, swap ith and last
      // element
      else {
        int temp = a[i];
        a[i] = a[size - 1];
        a[size - 1] = temp;
      }
    }
  }

  public List<int[]> getPermutations() {
    return this.permutations;
  }
}