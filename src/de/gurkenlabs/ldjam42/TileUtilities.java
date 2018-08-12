package de.gurkenlabs.ldjam42;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.gurkenlabs.ldjam42.entities.PartyGuest;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.environment.tilemap.MapUtilities;
import de.gurkenlabs.litiengine.util.ArrayUtilities;

public final class TileUtilities {

  private TileUtilities() {
  }

  public static void centerEntityInCurrentTile(ICollisionEntity entity) {
    int tileWidth = Game.getEnvironment().getMap().getTileSize().width;
    int tileHeight = Game.getEnvironment().getMap().getTileSize().height;
    Point currentTile = MapUtilities.getTile(entity.getCollisionBoxCenter());
    double x = currentTile.x * tileWidth + (tileWidth / 2.0 - entity.getWidth() / 2.0);
    double y = currentTile.y * tileHeight + (tileHeight / 2.0 - entity.getHeight() / 2.0);
    entity.setLocation(x, y);
  }

  public static List<Point> getTiles(Rectangle2D area) {
    Point start = MapUtilities.getTile(Game.getEnvironment().getMap(), area.getX(), area.getY());
    Point end = MapUtilities.getTile(Game.getEnvironment().getMap(), area.getMaxX(), area.getMaxY());
    ArrayList<Point> tiles = new ArrayList<>();
    for (int x = start.x; x <= end.x; x++) {
      for (int y = start.y; y <= end.y; y++) {
        tiles.add(new Point(x, y));
      }
    }

    return tiles;
  }

  public static List<Point> getEmptyTiles(Rectangle2D area) {
    Point start = MapUtilities.getTile(Game.getEnvironment().getMap(), area.getX(), area.getY());
    Point end = MapUtilities.getTile(Game.getEnvironment().getMap(), area.getMaxX(), area.getMaxY());
    ArrayList<Point> tiles = new ArrayList<>();
    for (int x = start.x; x <= end.x; x++) {
      for (int y = start.y; y <= end.y; y++) {
        boolean guestOnTile = false;
        for (PartyGuest guest : Game.getEnvironment().getByType(PartyGuest.class)) {
          if (guest.getCollisionBox().intersects(MapUtilities.getTileBoundingBox(x, y))) {
            guestOnTile = true;
            break;
          }
        }
        if (!guestOnTile) {
          tiles.add(new Point(x, y));
        }
      }
    }

    return tiles;
  }

  public static Map<Point, Boolean> getTilesWithOccupation(Rectangle2D area) {
    Point start = MapUtilities.getTile(Game.getEnvironment().getMap(), area.getX(), area.getY());
    Point end = MapUtilities.getTile(Game.getEnvironment().getMap(), area.getMaxX(), area.getMaxY());
    Map<Point, Boolean> tiles = new HashMap<>();
    for (int x = start.x; x <= end.x; x++) {
      for (int y = start.y; y <= end.y; y++) {
        boolean guestOnTile = false;
        for (PartyGuest guest : Game.getEnvironment().getByType(PartyGuest.class)) {
          if (guest.getCollisionBox().intersects(MapUtilities.getTileBoundingBox(x, y))) {
            guestOnTile = true;
            break;
          }
        }

        tiles.put(new Point(x, y), guestOnTile);

      }
    }

    return tiles;
  }

  public static Point2D getRandomTileCenterLocation(Rectangle2D area) {
    List<Point> tiles = getEmptyTiles(area);
    if (tiles.isEmpty()) {
      tiles = getTiles(area);
    }

    Point randomTile = ArrayUtilities.getRandom(tiles.toArray(new Point[tiles.size()]));
    Rectangle2D bounds = MapUtilities.getTileBoundingBox(randomTile);
    return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
  }
}
