package com.ecosim.model;

import com.ecosim.util.Constants;
import com.ecosim.util.Vector2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Bản đồ thế giới - lưới 2D gồm các TerrainTile.
 * Kết hợp 3 vùng: Đồng cỏ, Rừng rậm, Hồ nước.
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

    /**
     * Tạo bản đồ mặc định với 3 vùng chính + vùng chuyển tiếp.
     */
    private void generateDefaultMap() {
        // Bước 1: Đặt mặc định tất cả là đồng cỏ
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tiles[y][x] = new TerrainTile(TerrainType.GRASSLAND);
            }
        }

        // Bước 2: Tạo vùng rừng rậm (bên phải, trên)
        for (int y = Constants.FOREST_Y1; y < Math.min(Constants.FOREST_Y2, height); y++) {
            for (int x = Constants.FOREST_X1; x < Math.min(Constants.FOREST_X2, width); x++) {
                if (isInBounds(x, y)) {
                    // Rừng có xác suất chứa bụi rậm xen kẽ
                    if (random.nextDouble() < 0.15) {
                        tiles[y][x] = new TerrainTile(TerrainType.BUSH);
                    } else {
                        tiles[y][x] = new TerrainTile(TerrainType.FOREST);
                    }
                }
            }
        }

        // Bước 3: Tạo hồ nước (dưới giữa)
        for (int y = Constants.LAKE_Y1; y < Math.min(Constants.LAKE_Y2, height); y++) {
            for (int x = Constants.LAKE_X1; x < Math.min(Constants.LAKE_X2, width); x++) {
                if (isInBounds(x, y)) {
                    // Viền hồ có bùn
                    if (isLakeEdge(x, y)) {
                        tiles[y][x] = new TerrainTile(TerrainType.MUD);
                    } else {
                        tiles[y][x] = new TerrainTile(TerrainType.WATER);
                    }
                }
            }
        }

        // Bước 4: Rải một số vách đá ngẫu nhiên
        scatterRocks(30);

        // Bước 5: Thêm bụi rậm quanh rìa rừng và đồng cỏ
        addBushBorders();
    }

    /** Kiểm tra có phải viền hồ không */
    private boolean isLakeEdge(int x, int y) {
        int margin = 2;
        return x < Constants.LAKE_X1 + margin || x >= Constants.LAKE_X2 - margin
            || y < Constants.LAKE_Y1 + margin || y >= Constants.LAKE_Y2 - margin;
    }

    /** Rải đá ngẫu nhiên trên bản đồ */
    private void scatterRocks(int count) {
        for (int i = 0; i < count; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            if (tiles[y][x].getType() == TerrainType.GRASSLAND) {
                tiles[y][x] = new TerrainTile(TerrainType.ROCK);
                // Tạo cụm đá nhỏ (1-3 tile)
                int clusterSize = random.nextInt(3);
                for (int j = 0; j < clusterSize; j++) {
                    int nx = x + random.nextInt(3) - 1;
                    int ny = y + random.nextInt(3) - 1;
                    if (isInBounds(nx, ny) && tiles[ny][nx].getType() == TerrainType.GRASSLAND) {
                        tiles[ny][nx] = new TerrainTile(TerrainType.ROCK);
                    }
                }
            }
        }
    }

    /** Thêm bụi rậm ở ranh giới rừng/đồng cỏ */
    private void addBushBorders() {
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (tiles[y][x].getType() == TerrainType.GRASSLAND && hasAdjacentForest(x, y)) {
                    if (random.nextDouble() < 0.3) {
                        tiles[y][x] = new TerrainTile(TerrainType.BUSH);
                    }
                }
            }
        }
    }

    /** Kiểm tra xem ô có nằm cạnh rừng không */
    private boolean hasAdjacentForest(int x, int y) {
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        for (int[] d : dirs) {
            int nx = x + d[0], ny = y + d[1];
            if (isInBounds(nx, ny) && tiles[ny][nx].getType() == TerrainType.FOREST) {
                return true;
            }
        }
        return false;
    }

    // ===== Public API =====

    /** Lấy tile tại tọa độ (tile index) */
    public TerrainTile getTileAt(int x, int y) {
        if (!isInBounds(x, y)) return null;
        return tiles[y][x];
    }

    /** Lấy loại terrain tại tọa độ thế giới (double) */
    public TerrainType getTerrainAt(double worldX, double worldY) {
        int tx = (int) Math.floor(worldX);
        int ty = (int) Math.floor(worldY);
        if (!isInBounds(tx, ty)) return TerrainType.ROCK; // Ngoài bản đồ = không đi được
        return tiles[ty][tx].getType();
    }

    /** Kiểm tra tọa độ có nằm trong bản đồ */
    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /** Kiểm tra tọa độ thế giới có nằm trong bản đồ */
    public boolean isInBounds(double worldX, double worldY) {
        return worldX >= 0 && worldX < width && worldY >= 0 && worldY < height;
    }

    /** Đặt loại terrain tại vị trí (dùng cho user interaction) */
    public void setTerrainAt(int x, int y, TerrainType type) {
        if (isInBounds(x, y)) {
            tiles[y][x].setType(type);
        }
    }

    /** Tìm vị trí nguồn nước gần nhất từ vị trí cho trước */
    public Vector2D findNearestWater(Vector2D from) {
        double minDist = Double.MAX_VALUE;
        Vector2D nearest = null;

        // Scan theo vòng tròn mở rộng
        int cx = from.getTileX();
        int cy = from.getTileY();
        int maxRadius = Math.max(width, height);

        for (int r = 1; r < maxRadius; r++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dx = -r; dx <= r; dx++) {
                    if (Math.abs(dx) != r && Math.abs(dy) != r) continue; // Chỉ xét viền
                    int tx = cx + dx, ty = cy + dy;
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
            if (nearest != null) break; // Tìm thấy ở vòng hiện tại thì dừng
        }
        return nearest;
    }

    /** Tìm vị trí bụi rậm/rừng gần nhất (để trốn) */
    public Vector2D findNearestHidingSpot(Vector2D from) {
        double minDist = Double.MAX_VALUE;
        Vector2D nearest = null;

        int cx = from.getTileX();
        int cy = from.getTileY();
        int maxRadius = 20; // Tìm trong bán kính 20 tile

        for (int r = 1; r < maxRadius; r++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dx = -r; dx <= r; dx++) {
                    if (Math.abs(dx) != r && Math.abs(dy) != r) continue;
                    int tx = cx + dx, ty = cy + dy;
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

    /** Tìm vị trí ngẫu nhiên trên loại terrain chỉ định */
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

    /** Tìm vị trí ngẫu nhiên có thể đi được */
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
