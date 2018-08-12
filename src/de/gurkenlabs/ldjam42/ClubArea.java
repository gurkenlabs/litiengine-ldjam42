package de.gurkenlabs.ldjam42;

public enum ClubArea {
  LOBBY("area_entry"),
  DANCEFLOOR("area_dance"),
  CHILLAREA("area_chill"),
  BAR("area_drink"),
  PIZZASTAND("area_food");

  private String tag;

  private ClubArea(String tag) {
    this.tag = tag;
  }

  public String getTag() {
    return this.tag;
  }

  public boolean isMainArea() {
    return this != LOBBY;
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
