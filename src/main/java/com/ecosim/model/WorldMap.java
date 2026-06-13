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
    private final Random random;

    public WorldMap(int width, int height) {
        this(width, height, Constants.WORLD_SEED);
    }

    public WorldMap(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.random = new Random(seed);
        this.tiles = new TerrainTile[height][width];
        generateDefaultMap();
    }

    public WorldMap() {
        this(Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
    }

    /**
     * Tạo bản đồ mặc định với thuật toán hữu cơ (Cellular Automata).
     */
    private void generateDefaultMap() {
        // Bước 1: Khởi tạo toàn bộ là cỏ
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tiles[y][x] = new TerrainTile(TerrainType.GRASSLAND);
            }
        }

        // Bước 2: Tạo hạt giống (seeds) cho Rừng và Hồ nước
        double forestCx = width * 0.75, forestCy = height * 0.3;
        double lakeCx = width * 0.3, lakeCy = height * 0.7;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double distToForest = Math.hypot(x - forestCx, y - forestCy);
                double distToLake = Math.hypot(x - lakeCx, y - lakeCy);
                
                // Khởi tạo nhiễu ngẫu nhiên ban đầu
                if (distToForest < 20 + random.nextDouble() * 15 && random.nextDouble() < 0.55) {
                    tiles[y][x].setType(TerrainType.FOREST);
                }
                if (distToLake < 15 + random.nextDouble() * 10 && random.nextDouble() < 0.6) {
                    tiles[y][x].setType(TerrainType.WATER);
                }
            }
        }

        // Bước 3: Làm mượt bằng Cellular Automata (3 bước)
        for (int step = 0; step < 3; step++) {
            TerrainType[][] nextGen = new TerrainType[height][width];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int forestCount = countNeighbors(x, y, TerrainType.FOREST);
                    int waterCount = countNeighbors(x, y, TerrainType.WATER);
                    
                    TerrainType current = tiles[y][x].getType();
                    TerrainType next = current;
                    
                    if (current == TerrainType.FOREST) {
                        next = (forestCount >= 4) ? TerrainType.FOREST : TerrainType.GRASSLAND;
                    } else if (current == TerrainType.WATER) {
                        next = (waterCount >= 4) ? TerrainType.WATER : TerrainType.GRASSLAND;
                    } else { // Cỏ
                        if (forestCount >= 5) next = TerrainType.FOREST;
                        else if (waterCount >= 5) next = TerrainType.WATER;
                    }
                    nextGen[y][x] = next;
                }
            }
            // Áp dụng nextGen
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    tiles[y][x].setType(nextGen[y][x]);
                }
            }
        }

        // Bước 4: Tạo viền hồ bùn lầy tự nhiên
        TerrainType[][] finalGen = new TerrainType[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                finalGen[y][x] = tiles[y][x].getType();
                if (tiles[y][x].getType() == TerrainType.GRASSLAND) {
                    if (countNeighbors(x, y, TerrainType.WATER) > 0) {
                        finalGen[y][x] = TerrainType.MUD; // Cỏ cạnh nước biến thành bùn
                    } else if (countNeighbors(x, y, TerrainType.FOREST) > 0 && random.nextDouble() < 0.4) {
                        finalGen[y][x] = TerrainType.BUSH; // Cỏ cạnh rừng dễ thành bụi rậm
                    }
                }
            }
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tiles[y][x].setType(finalGen[y][x]);
            }
        }

        // Bước 5: Rải đá ngẫu nhiên trên đồng cỏ
        scatterRocks(35);
    }

    /** Đếm số ô neighbor xung quanh (bán kính 1) có loại type nhất định */
    private int countNeighbors(int x, int y, TerrainType type) {
        int count = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx, ny = y + dy;
                // Nếu ngoài viền, giả sử không phải type đó
                if (isInBounds(nx, ny) && tiles[ny][nx].getType() == type) {
                    count++;
                }
            }
        }
        return count;
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

    /** Kiểm tra tọa độ thế giới có nằm trong bản đồ (với lề để không sát mép) */
    public boolean isInBounds(double worldX, double worldY) {
        double margin = 1.5;
        return worldX >= margin && worldX <= width - margin && worldY >= margin && worldY <= height - margin;
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

    /** Tìm vị trí bụi rậm gần nhất (để trốn) */
    public Vector2D findNearestHidingSpot(Vector2D from) {
        double minBushDist = Double.MAX_VALUE;
        Vector2D nearestBush = null;

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
                        if (t == TerrainType.BUSH) {
                            Vector2D hidePos = new Vector2D(tx + 0.5, ty + 0.5);
                            double dist = from.distanceTo(hidePos);
                            if (dist < minBushDist) {
                                minBushDist = dist;
                                nearestBush = hidePos;
                            }
                        }
                    }
                }
            }
            if (nearestBush != null) return nearestBush;
        }
        return null;
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
