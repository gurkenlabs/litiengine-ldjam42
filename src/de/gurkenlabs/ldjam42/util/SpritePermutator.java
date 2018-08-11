package de.gurkenlabs.ldjam42.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import de.gurkenlabs.ldjam42.constants.GoInSprites;
import de.gurkenlabs.litiengine.util.ImageProcessing;

public class SpritePermutator {
  private List<int[]> permutations;
  private int maxNumberOfFeatures;
  private Image[][] features;
  private List<Image> permutatedImages;

  public SpritePermutator(Image[][] features) {
    this.features = features;
    this.permutations = new ArrayList<>();
    this.permutatedImages = new ArrayList<>();
    this.maxNumberOfFeatures = this.features[0].length;
    this.generateIndexPermutations();
    this.generateSpritePermutations();
  }

  private void generateIndexPermutations() {
    int[] possibleIndices = new int[this.maxNumberOfFeatures];
    for (int i = 0; i < this.maxNumberOfFeatures; i++) {
      possibleIndices[i] = i;
    }
    this.heapPermutation(possibleIndices, this.maxNumberOfFeatures, this.maxNumberOfFeatures);
    // debug print
    System.out.println("Total permutations: " + this.permutations.size());
    for (int[] p : this.permutations) {
      String s = "";
      for (int pi : p) {
        s += pi + " ";
      }
      System.out.println(s);
    }
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

  private void generateSpritePermutations() {
    for (int permutationIndex = 0; permutationIndex < this.permutations.size(); permutationIndex++) {
      Image img = ImageProcessing.getCompatibleImage(this.features[0][0].getWidth(null), this.features[0][0].getHeight(null));
      Graphics2D g = (Graphics2D) img.getGraphics();
      g.drawImage(GoInSprites.IDLE_DOWN.getImage(), 0, 0, null);
      int typeIndex = 0;
      for (int featureIndex : this.permutations.get(permutationIndex)) {
        System.out.println("permutationIndex: " + permutationIndex + " pindex: " + typeIndex + " - featureIndex: " + featureIndex);
        g.drawImage(this.features[typeIndex][featureIndex], 0, 0, null);
        typeIndex++;
      }
      System.out.println("-");
      g.dispose();
      this.permutatedImages.add(img);
    }
  }

  public Image getSpritePermutationsInOneImage() {
    Image allPermutations = ImageProcessing.getCompatibleImage(this.getSpritePermutations().get(0).getWidth(null), this.getSpritePermutations().get(0).getHeight(null) * this.getSpritePermutations().size());
    Graphics2D g = (Graphics2D) allPermutations.getGraphics();

    for (int i = 0; i < this.getSpritePermutations().size(); i++) {
      g.drawImage(this.getSpritePermutations().get(i), 0, i * this.getSpritePermutations().get(0).getHeight(null), null);
    }
    g.dispose();
    return allPermutations;
  }

  public List<Image> getSpritePermutations() {
    return this.permutatedImages;
  }
}