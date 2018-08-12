package de.gurkenlabs.ldjam42.entities;

public class SkinManager {
  private int pants, top, face, hair;
  private Gender gender;

  public SkinManager(PartyGuest guest) {
    // guest.setSkinManager(this);
    this.gender = guest.getGender();
//  this.pants=guest.getFeatures().get(0);
//  this.top=guest.getFeatures().get(1);
//  this.face=guest.getFeatures().get(2);
//  this.hair=guest.getFeatures().get(3);

  }

  public String getPantsString() {
    return "";
  }

  public String getHairString() {
    return "";
  }

  public String getTopString() {
    return "";
  }

  public String getFaceString() {
    return "";
  }

}
