package de.gurkenlabs.ldjam42.entities;

public class SkinManager {
  private int pants, top, face, hair;
  private Gender gender;
  
  public SkinManager(PartyGuest guest) {
    //guest.setSkinManager(this);
    this.gender=guest.getGender();
    
  }
  
  public int getPants() {
    return pants;
  }
  public void setPants(int pants) {
    this.pants = pants;
  }
  public Gender getGender() {
    return gender;
  }
  public void setGender(Gender gender) {
    this.gender = gender;
  }
  public int getHair() {
    return hair;
  }
  public void setHair(int hair) {
    this.hair = hair;
  }
  public int getTop() {
    return top;
  }
  public void setTop(int top) {
    this.top = top;
  }
  public int getFace() {
    return face;
  }
  public void setFace(int face) {
    this.face = face;
  }
  
}
