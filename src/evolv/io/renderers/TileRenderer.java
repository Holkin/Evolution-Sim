package evolv.io.renderers;


import evolv.io.Configuration;
import evolv.io.model.World;
import processing.core.PApplet;

import java.util.HashMap;
import java.util.Map;

import static evolv.io.util.ColorUtil.*;
import static evolv.io.Configuration.*;

public class TileRenderer {

    private final PApplet applet;
    private final int waterColor;
    private final int blackColor;
    private final int barrenColor;
    private final int fertileColor;

    // since climate is constant, we can have some cache
    private Map<World.Tile, Integer> climateCache = new HashMap<>();

    public TileRenderer(PApplet pApplet) {
        this.applet = pApplet;
        waterColor = applet.color(0, 0, 0);
        blackColor = applet.color(0, 1, 0);
        barrenColor = applet.color(0, 0, 1);
        fertileColor = applet.color(0, 0, 0.2f);
    }

    public void drawTile(World.Tile tile, float scaleUp) {
        applet.stroke(0, 0, 0, 1);
        applet.strokeWeight(2);
        applet.fill(pickColorFor(tile));
        applet.rect(tile.x * scaleUp, tile.y * scaleUp, scaleUp, scaleUp);
    }

    public void drawTileInfo(World.Tile tile, float scaleUp, float camZoom) {
        if (camZoom > Configuration.MAX_DETAILED_ZOOM) {
            if (this.applet.brightness(pickColorFor(tile)) >= 0.7f) {
                this.applet.fill(0, 0, 0, 1);
            } else {
                this.applet.fill(0, 0, 1, 1);
            }
            this.applet.textAlign(PApplet.CENTER);
            this.applet.textSize(21);
            this.applet.text(PApplet.nf((float) (100 * tile.getFood()), 0, 2) + " yums", (tile.x + 0.5f) * scaleUp,
                    (tile.y + 0.3f) * scaleUp);
            this.applet.text("Clim: " + PApplet.nf(tile.getClimate(), 0, 2), (tile.x + 0.5f) * scaleUp,
                    (tile.y + 0.6f) * scaleUp);
            this.applet.text("Food: " + PApplet.nf((float) tile.getFoodType(), 0, 2), (tile.x + 0.5f) * scaleUp,
                    (tile.y + 0.9f) * scaleUp);
        }
    }

    public int getFoodColor(World.Tile tile) {
        Integer type = climateCache.get(tile);
        if (type == null) {
            type = applet.color(tile.getClimate(), 1, 1);
            climateCache.put(tile, type);
        }
        return type;
    }

    private int pickColorFor(World.Tile tile) {
        if (tile.isWater()) {
            return waterColor;
        }
        int foodColor = getFoodColor(tile);
        double foodLevel = tile.getFood();
        if (foodLevel < MAX_GROWTH_LEVEL) {
            int interColor = interColor(barrenColor, fertileColor, tile.getFertility(), applet);
            double growthPercentage = foodLevel / MAX_GROWTH_LEVEL;
            return interColorFixedHue(interColor, foodColor, growthPercentage, applet.hue(foodColor), applet);
        }
        double x = 1.0 - MAX_GROWTH_LEVEL / foodLevel;
        return interColorFixedHue(foodColor, blackColor, x, applet.hue(foodColor), applet);
    }
}
