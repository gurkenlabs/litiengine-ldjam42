package de.gurkenlabs.ldjam42.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.gurkenlabs.ldjam42.GameManager;
import de.gurkenlabs.ldjam42.entities.PartyGuest;
import de.gurkenlabs.litiengine.graphics.ImageEffect;
import de.gurkenlabs.litiengine.graphics.ImageRenderer;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.util.ImageProcessing;

public class FocusImageEffect extends ImageEffect {
  private static final Color HOVER_COLOR = new Color(255, 255, 255, 70);
  private static final Color TARGET_COLOR = new Color(255, 0, 0, 200);
  private final PartyGuest guest;

  public FocusImageEffect(PartyGuest guest) {
    super(0, "focus");
    this.guest = guest;
  }

  @Override
  public BufferedImage apply(BufferedImage image) {
    if (!this.isFocusesed() && !this.isHovered()) {
      return image;
    }

    int newImageWidth = image.getWidth() + 2;
    int newImageHeight = image.getHeight() + 2;
    float offsetX = (newImageWidth - image.getWidth()) / 2.0f;
    float offsetY = (newImageHeight - image.getHeight()) / 2.0f;

    final BufferedImage buffer = ImageProcessing.getCompatibleImage(newImageWidth, newImageHeight);
    final Graphics2D graphics = buffer.createGraphics();

    Color color = this.isFocusesed() ? TARGET_COLOR : HOVER_COLOR;
    final BufferedImage borderImage = ImageProcessing.borderAlpha(image, color, false);
    ImageRenderer.render(graphics, borderImage, offsetX - 1, offsetY - 1);
    graphics.dispose();

    return buffer;
  }

  @Override
  public String getName() {
    if (this.isFocusesed()) {
      return "focus";
    }

    if (this.isHovered()) {
      return "hover";
    }

    return "no-focus";
  }

  private boolean isFocusesed() {
    return GameManager.getCurrentFocus() != null && GameManager.getCurrentFocus().equals(this.guest);
  }

  private boolean isHovered() {
    return this.guest.getBoundingBox().contains(Input.mouse().getMapLocation());
  }
}
