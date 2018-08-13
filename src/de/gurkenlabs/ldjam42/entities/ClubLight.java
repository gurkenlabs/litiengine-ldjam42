package de.gurkenlabs.ldjam42.entities;

import java.awt.Color;
import java.awt.Graphics2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.LightSource;
import de.gurkenlabs.litiengine.util.MathUtilities;

public class ClubLight extends LightSource {
  private int minOnTime;
  private int maxOnTime;
  private int minOffTime;
  private int maxOffTime;

  private long pulseStart;
  private int pulseDuration;
  private boolean alphaIncreasing;
  private long nextToggle;
  private int alphaPreset;
  private float currentAlpha;

  public ClubLight(int intensity, Color lightColor, String shapeType, boolean activated) {
    super(intensity, lightColor, shapeType, activated);
    this.alphaPreset = this.getColor().getAlpha();
  }

  public int getMinOnTime() {
    return this.minOnTime;
  }

  public void setMinOnTime(int minOnTime) {
    this.minOnTime = minOnTime;
  }

  public int getMaxOnTime() {
    return this.maxOnTime;
  }

  public void setMaxOnTime(int maxOnTime) {
    this.maxOnTime = maxOnTime;
  }

  public int getMinOffTime() {
    return this.minOffTime;
  }

  public void setMinOffTime(int minOffTime) {
    this.minOffTime = minOffTime;
  }

  public int getMaxOffTime() {
    return this.maxOffTime;
  }

  public void setMaxOffTime(int maxOffTime) {
    this.maxOffTime = maxOffTime;
  }

  public int getPulseDuration() {
    return this.pulseDuration;
  }

  public void setPulseDuration(int pulseDuration) {
    this.pulseDuration = pulseDuration;
  }

  @Override
  public void toggle() {
    super.toggle();
    if (this.isActive()) {
      final int onTime = MathUtilities.randomInRange(this.getMinOnTime(), this.getMaxOnTime());
      this.nextToggle = onTime + Game.getTime().sinceEnvironmentLoad();
    } else {
      final int offTime = MathUtilities.randomInRange(this.getMinOffTime(), this.getMaxOffTime());
      this.nextToggle = offTime + Game.getTime().sinceEnvironmentLoad();
    }
  }

  @Override
  public void render(Graphics2D g) {
    super.render(g);

    if (!this.isPulsateEnabled()) {
      return;
    }

    if (Game.getTime().sinceEnvironmentLoad() >= this.nextToggle) {
      if (this.getPulseDuration() == 0) {
        this.toggle();
      } else {
        int remainingPulse = (int) MathUtilities.clamp(this.getPulseDuration() - (Game.getTime().sinceEnvironmentLoad() - this.pulseStart), 0, this.getPulseDuration());
        if (remainingPulse == 0 || remainingPulse == this.getPulseDuration()) {
          this.pulsate();
        }
      }
    }
    if (this.getPulseDuration() == 0) {
      return;
    }
    this.handlePulse();
  }

  private void handlePulse() {
    if (Game.getEnvironment() == null || Game.getEnvironment().getAmbientLight() == null) {
      return;
    }
    if (this.pulseStart != -1) {
      final long timePassed = Game.getTime().sinceEnvironmentLoad() - this.pulseStart;
      if (this.alphaIncreasing) {
        this.currentAlpha = MathUtilities.clamp(timePassed / (float) this.getPulseDuration(), 0, 1);
      } else {
        this.currentAlpha = MathUtilities.clamp(1 - (timePassed / (float) this.getPulseDuration()), 0, 1);
      }

      // System.out.println("Time passed: " + timePassed + " | remaining pulse: " + (this.getPulseDuration() - timePassed) + " | Current Alpha: " + this.currentAlpha);

      if (this.currentAlpha == 1.0) {
        this.pulseStart = -1;
        this.currentAlpha = -1;
      }
    }
    if (this.currentAlpha != -1) {
      Color c = new Color((int) (this.getColor().getRed()), (int) (this.getColor().getGreen()), (int) (this.getColor().getBlue()), (int) (this.currentAlpha * this.alphaPreset));
      this.setColor(c);
      Game.getEnvironment().getAmbientLight().updateSection(this.getBoundingBox());
    }

  }

  public void pulsate() {
    if ((int) (this.currentAlpha * 255) == this.alphaPreset) {
      final int onTime = MathUtilities.randomInRange(this.getMinOnTime(), this.getMaxOnTime());
      this.nextToggle = onTime + Game.getTime().sinceEnvironmentLoad();
      this.alphaIncreasing = false;
    } else if (this.currentAlpha == 0) {
      final int offTime = MathUtilities.randomInRange(this.getMinOffTime(), this.getMaxOffTime());
      this.nextToggle = offTime + Game.getTime().sinceEnvironmentLoad();
      this.alphaIncreasing = true;
    }
    this.pulseStart = Game.getTime().sinceEnvironmentLoad();
  }

  public boolean isPulsateEnabled() {
    return this.getMinOnTime() > 0 && this.getMaxOnTime() > 0 && this.getMinOffTime() > 0 && this.getMaxOffTime() > 0;
  }
}
