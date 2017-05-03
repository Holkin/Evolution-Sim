package evolv.io.model;

import evolv.io.iface.FoodGenerator;
import evolv.io.iface.MapGenerator;

import static evolv.io.Configuration.*;

/**
 * Represents world in which creatures can live.
 * It is rectangular tiled surface.
 * Encapsulates data in tables, which is expected to perform better than a lot of separate objects.
 */

public class World {

    private static int NEXT_TILE_ID = 0;

    private final int width;
    private final int height;
    private final double fertility[][];
    private final double climate[][];
    private final double food[][];
    private final Tile tiles[][];

    private FoodGenerator foodGen;

    public World(int width, int height) {
        this.width = width;
        this.height = height;
        fertility = new double[height][width];
        climate = new double[height][width];
        food = new double[height][width];
        tiles = new Tile[height][width];
        // generate once
        for (int i=0; i<height; i++) {
            for (int j=0; j<width; j++) {
                tiles[i][j] = new Tile(i, j);
            }
        }
    }

    public Tile getTile(int x, int y) {
        return tiles[y][x];
    }

    /**
     * This is expected to be called once. No performance optimisation required.
     */
    public void recreate(MapGenerator climateGen, MapGenerator fertilityGen) {
        for (int i=0; i<height; i++) {
            for (int j=0; j<width; j++) {
                climate[i][j] = climateGen.generate(i, j);
                fertility[i][j] = Math.max(fertilityGen.generate(i, j), 0);
                food[i][j] = fertility[i][j];
            }
        }
    }

    /**
     * This is expected to be called for every frame.
     */
    public void update(double growthRate) {
        for (int i=0; i<height; i++) {
            for (int j=0; j<width; j++) {
                if (fertility[i][j] > MAX_FERTILITY) continue;
                food[i][j] += foodGen.<Double>generate(fertility[i][j], food[i][j], growthRate);
                food[i][j] = Math.max(food[i][j], 0);
            }
        }
    }

    public void setFoodGen(FoodGenerator foodGen) {
        this.foodGen = foodGen;
    }

    /**
     * This is expected to be called for every frame by renderer.
     * // TODO find solution to query only visible tiles
     */
    public Tile[][] getTiles() {
        return tiles;
    }

    private static int getTileId() {
        return ++NEXT_TILE_ID;
    }

    /**
     * Provides view to table data.
     */
    public class Tile {
        public final int x, y, id;
        Tile(int x, int y) {
            this.x = x;
            this.y = y;
            this.id = getTileId();
        }
        public double getFood() {
            return food[y][x];
        }
        public float getClimate() {
            return (float) climate[y][x];
        }
        public float getFertility() {
            return (float) fertility[y][x];
        }
        public boolean isWater() {
            return fertility[y][x] > MAX_FERTILITY;
        }
        public void incFood(double amount) {
            food[y][x] = Math.max(food[y][x] + amount, 0);
        }
        public double getFoodType() {
            return getClimate();
        }

        // allows using tile as Map key
        public boolean equals(Object o) {
            return o instanceof Tile && ((Tile) o).id == this.id;
        }

        // allows using tile as Map key
        public int hashCode() {
            return id;
        }
    }
}
