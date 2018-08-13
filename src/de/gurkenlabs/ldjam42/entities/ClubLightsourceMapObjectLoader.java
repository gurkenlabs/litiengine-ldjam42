package de.gurkenlabs.ldjam42.entities;

import java.awt.Color;

import de.gurkenlabs.ldjam42.constants.GoInMapObjectProperties;
import de.gurkenlabs.litiengine.entities.LightSource;
import de.gurkenlabs.litiengine.environment.LightSourceMapObjectLoader;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;

public class ClubLightsourceMapObjectLoader extends LightSourceMapObjectLoader {

  public ClubLightsourceMapObjectLoader() {
    super();
  }

  @Override
  protected LightSource createLightSource(IMapObject mapObject, int intensity, Color color, String lightType, boolean active) {
    ClubLight light = new ClubLight(intensity, color, lightType, active);
    final int minOnTime = mapObject.getCustomPropertyInt(GoInMapObjectProperties.LIGHT_ON_MIN);
    final int maxOnTime = mapObject.getCustomPropertyInt(GoInMapObjectProperties.LIGHT_ON_MAX);
    final int minOffTime = mapObject.getCustomPropertyInt(GoInMapObjectProperties.LIGHT_OFF_MIN);
    final int maxOffTime = mapObject.getCustomPropertyInt(GoInMapObjectProperties.LIGHT_OFF_MAX);
    final int pulseDuration = mapObject.getCustomPropertyInt(GoInMapObjectProperties.LIGHT_PULSE_DURATION);

    light.setMinOnTime(minOnTime);
    light.setMaxOnTime(maxOnTime);
    light.setMinOffTime(minOffTime);
    light.setMaxOffTime(maxOffTime);
    light.setPulseDuration(pulseDuration);
    return light;
  }

}
