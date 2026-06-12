package com.ecosim.model;

import com.ecosim.util.Constants;
import com.ecosim.util.Vector2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 2D world map made of terrain tiles.
 * The map keeps the same broad regions as before, but uses organic shapes.
 */
public class WorldMap {
    private final int width;
    private final int height;
    private final TerrainTile[][] tiles;
    private final Random random = new Random();

    public WorldMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new TerrainTile[height][width];
        generateDefaultMap();
    }

    public WorldMap() {
        this(Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
    }

    private void generateDefaultMap() {
        fillGrassland();
        createOrganicForest();
        createOrganicLake();
        createPond(18, 25, 7, 5);
        createPond(92, 63, 8, 4);
        addMudAroundWater();
        scatterRocks(30);
        addBushBorders();
    }

    private void fillGrassland() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tiles[y][x] = new TerrainTile(TerrainType.GRASSLAND);
            }
        }
    }

    private void createOrganicForest() {
        double centerX = (Constants.FOREST_X1 + Constants.FOREST_X2) / 2.0;
        double centerY = (Constants.FOREST_Y1 + Constants.FOREST_Y2) / 2.0;
        double radiusX = (Constants.FOREST_X2 - Constants.FOREST_X1) / 2.0;
        double radiusY = (Constants.FOREST_Y2 - Constants.FOREST_Y1) / 2.0;

        int startX = Math.max(0, Constants.FOREST_X1 - 10);
        int endX = Math.min(width, Constants.FOREST_X2 + 6);
        int startY = Math.max(0, Constants.FOREST_Y1 - 8);
        int endY = Math.min(height, Constants.FOREST_Y2 + 8);

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                double nx = (x - centerX) / radiusX;
                double ny = (y - centerY) / radiusY;
                double value = nx * nx + ny * ny + organicNoise(x, y, 0.18);

                if (value < 1.03) {
                    setTerrain(x, y, random.nextDouble() < 0.14 ? TerrainType.BUSH : TerrainType.FOREST);
                } else if (value < 1.22 && random.nextDouble() < 0.42) {
                    setTerrain(x, y, random.nextDouble() < 0.55 ? TerrainType.BUSH : TerrainType.FOREST);
                }
            }
        }
    }

    private void createOrganicLake() {
        double centerX = (Constants.LAKE_X1 + Constants.LAKE_X2) / 2.0;
        double centerY = (Constants.LAKE_Y1 + Constants.LAKE_Y2) / 2.0;
        double radiusX = (Constants.LAKE_X2 - Constants.LAKE_X1) / 2.0;
        double radiusY = (Constants.LAKE_Y2 - Constants.LAKE_Y1) / 2.0;

        int startX = Math.max(0, Constants.LAKE_X1 - 6);
        int endX = Math.min(width, Constants.LAKE_X2 + 6);
        int startY = Math.max(0, Constants.LAKE_Y1 - 5);
        int endY = Math.min(height, Constants.LAKE_Y2 + 5);

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                double nx = (x - centerX) / radiusX;
                double ny = (y - centerY) / radiusY;
                double value = nx * nx + ny * ny + organicNoise(x + 31, y + 17, 0.20);

                if (value < 0.78) {
                    setTerrain(x, y, TerrainType.WATER);
                } else if (value < 1.08) {
                    setTerrain(x, y, TerrainType.MUD);
                }
            }
        }
    }

    private void createPond(int centerX, int centerY, int radiusX, int radiusY) {
        int startX = Math.max(0, centerX - radiusX - 2);
        int endX = Math.min(width - 1, centerX + radiusX + 2);
        int startY = Math.max(0, centerY - radiusY - 2);
        int endY = Math.min(height - 1, centerY + radiusY + 2);

        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                double nx = (x - centerX) / (double) radiusX;
                double ny = (y - centerY) / (double) radiusY;
                double value = nx * nx + ny * ny + organicNoise(x + centerX, y + centerY, 0.16);

                if (value < 0.78) {
                    setTerrain(x, y, TerrainType.WATER);
                } else if (value < 1.08 && getTileAt(x, y).getType() != TerrainType.WATER) {
                    setTerrain(x, y, TerrainType.MUD);
                }
            }
        }
    }

    private void addMudAroundWater() {
        List<int[]> mudTiles = new ArrayList<>();

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (tiles[y][x].getType() == TerrainType.GRASSLAND
                    && hasAdjacentTerrain(x, y, TerrainType.WATER)) {

                    mudTiles.add(new int[] {x, y});
                }
            }
        }

        for (int[] tile : mudTiles) {
            setTerrain(tile[0], tile[1], TerrainType.MUD);
        }
    }

    private void scatterRocks(int count) {
        for (int i = 0; i < count; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            if (tiles[y][x].getType() == TerrainType.GRASSLAND) {
                setTerrain(x, y, TerrainType.ROCK);

                int clusterSize = random.nextInt(3);
                for (int j = 0; j < clusterSize; j++) {
                    int nx = x + random.nextInt(3) - 1;
                    int ny = y + random.nextInt(3) - 1;
                    if (isInBounds(nx, ny) && tiles[ny][nx].getType() == TerrainType.GRASSLAND) {
                        setTerrain(nx, ny, TerrainType.ROCK);
                    }
                }
            }
        }
    }

    private void addBushBorders() {
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (tiles[y][x].getType() == TerrainType.GRASSLAND && hasAdjacentForest(x, y)) {
                    if (random.nextDouble() < 0.38) {
                        setTerrain(x, y, TerrainType.BUSH);
                    }
                }
            }
        }
    }

    private boolean hasAdjacentForest(int x, int y) {
        return hasAdjacentTerrain(x, y, TerrainType.FOREST);
    }

    private boolean hasAdjacentTerrain(int x, int y, TerrainType type) {
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx;
                int ny = y + dy;
                if (isInBounds(nx, ny) && tiles[ny][nx].getType() == type) {
                    return true;
                }
            }
        }

        return false;
    }

    private double organicNoise(int x, int y, double amount) {
        double wave = Math.sin(x * 0.23) * 0.5 + Math.cos(y * 0.19) * 0.5;
        double hash = ((x * 928371 + y * 12377) & 0xFF) / 255.0 - 0.5;
        return (wave * 0.5 + hash) * amount;
    }

    private void setTerrain(int x, int y, TerrainType type) {
        if (isInBounds(x, y)) {
            tiles[y][x] = new TerrainTile(type);
        }
    }

    public TerrainTile getTileAt(int x, int y) {
        if (!isInBounds(x, y)) return null;
        return tiles[y][x];
    }

    public TerrainType getTerrainAt(double worldX, double worldY) {
        int tx = (int) Math.floor(worldX);
        int ty = (int) Math.floor(worldY);
        if (!isInBounds(tx, ty)) return TerrainType.ROCK;
        return tiles[ty][tx].getType();
    }

    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public boolean isInBounds(double worldX, double worldY) {
        return worldX >= 0 && worldX < width && worldY >= 0 && worldY < height;
    }

    public void setTerrainAt(int x, int y, TerrainType type) {
        if (isInBounds(x, y)) {
            tiles[y][x].setType(type);
        }
    }

    public Vector2D findNearestWater(Vector2D from) {
        double minDist = Double.MAX_VALUE;
        Vector2D nearest = null;

        int cx = from.getTileX();
        int cy = from.getTileY();
        int maxRadius = Math.max(width, height);

        for (int r = 1; r < maxRadius; r++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dx = -r; dx <= r; dx++) {
                    if (Math.abs(dx) != r && Math.abs(dy) != r) continue;
                    int tx = cx + dx;
                    int ty = cy + dy;
                    if (isInBounds(tx, ty) && tiles[ty][tx].getType() == TerrainType.WATER) {
                        Vector2D waterPos = new Vector2D(tx + 0.5, ty + 0.5);
                        double dist = from.distanceTo(waterPos);
                        if (dist < minDist) {
                            minDist = dist;
                            nearest = waterPos;
                        }
                    }
                }
            }
            if (nearest != null) break;
        }
        return nearest;
    }

    public Vector2D findNearestHidingSpot(Vector2D from) {
        double minDist = Double.MAX_VALUE;
        Vector2D nearest = null;

        int cx = from.getTileX();
        int cy = from.getTileY();
        int maxRadius = 20;

        for (int r = 1; r < maxRadius; r++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dx = -r; dx <= r; dx++) {
                    if (Math.abs(dx) != r && Math.abs(dy) != r) continue;
                    int tx = cx + dx;
                    int ty = cy + dy;
                    if (isInBounds(tx, ty)) {
                        TerrainType t = tiles[ty][tx].getType();
                        if (t == TerrainType.BUSH || t == TerrainType.FOREST) {
                            Vector2D hidePos = new Vector2D(tx + 0.5, ty + 0.5);
                            double dist = from.distanceTo(hidePos);
                            if (dist < minDist) {
                                minDist = dist;
                                nearest = hidePos;
                            }
                        }
                    }
                }
            }
            if (nearest != null) break;
        }
        return nearest;
    }

    public Vector2D getRandomPosition(TerrainType type) {
        List<Vector2D> candidates = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (tiles[y][x].getType() == type) {
                    candidates.add(new Vector2D(x + 0.5, y + 0.5));
                }
            }
        }
        if (candidates.isEmpty()) return new Vector2D(width / 2.0, height / 2.0);
        return candidates.get(random.nextInt(candidates.size()));
    }

    public Vector2D getRandomTraversablePosition() {
        for (int attempt = 0; attempt < 1000; attempt++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            TerrainType t = tiles[y][x].getType();
            if (t.isDefaultTraversable()) {
                return new Vector2D(x + 0.5, y + 0.5);
            }
        }
        return new Vector2D(width / 2.0, height / 2.0);
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public TerrainTile[][] getTiles() { return tiles; }
}
