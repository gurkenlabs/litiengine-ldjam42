package de.gurkenlabs.ldjam42;

public enum ClubArea {
  LOBBY("area_entry", null),
  DANCEFLOOR("area_dance", Needs.DANCE),
  CHILLAREA("area_chill", Needs.CHILL),
  BAR("area_drink", Needs.DRINK),
  PIZZASTAND("area_food", Needs.EAT);

  private String tag;
  private Needs need;

  private ClubArea(String tag, Needs need) {
    this.tag = tag;
    this.need = need;
  }

  public String getTag() {
    return this.tag;
  }

  public Needs getNeed() {
    return this.need;
  }

  public static ClubArea getArea(Needs need) {
    if (need == null) {
      return ClubArea.LOBBY;
    }

    for (ClubArea ar : ClubArea.values()) {
      if (ar.getNeed() == need) {
        return ar;
      }
    }

    return ClubArea.LOBBY;
  }

  public static ClubArea getAreaByTag(String tag) {
    if (tag == null || tag.isEmpty()) {
      return ClubArea.LOBBY;
    }

    for (ClubArea ar : ClubArea.values()) {
      if (ar.getTag().equalsIgnoreCase(tag)) {
        return ar;
      }
    }

    return ClubArea.LOBBY;
  }
}
