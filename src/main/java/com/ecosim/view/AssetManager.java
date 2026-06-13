package com.ecosim.view;

import com.ecosim.model.Entity;
import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;

/**
 * Quản lý tải và lưu trữ tài nguyên hình ảnh (Sprites / GIFs).
 * Hỗ trợ fallback: Nếu không tìm thấy file, trả về null để sử dụng hình học cơ bản.
 */
public class AssetManager {

    private static AssetManager instance;
    private final Map<String, Image> cache = new HashMap<>();
    private boolean debugLogged = false;
    private boolean generatedSpritesLoaded = false;

    private AssetManager() {}

    public static AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }

    /**
     * Lấy ảnh dựa trên tên loại và trạng thái của động vật.
     * Cố gắng tìm file ví dụ: /sprites/rabbit_run.gif hoặc /sprites/rabbit.png
     */
    public Image getSprite(Entity entity) {
        // Tự động sinh sprite nếu chưa có
        if (!generatedSpritesLoaded) {
            generatedSpritesLoaded = true;
            SpriteGenerator.generateAll();
        }

        String typeName = entity.getClass().getSimpleName().toLowerCase();
        String stateSuffix = "";

        if (entity instanceof com.ecosim.model.Animal animal) {
            stateSuffix = switch (animal.getState()) {
                case RUNNING, FLEEING -> "_run";
                case EATING -> "_eat";
                case SLEEPING -> "_sleep";
                default -> "_idle";
            };
        }

        // Ưu tiên load GIF (ảnh động)
        String gifKey = typeName + stateSuffix + ".gif";
        Image img = loadFromClasspath(gifKey);
        
        if (img == null) {
            // Fallback sang ảnh tĩnh PNG theo trạng thái
            String pngKey = typeName + stateSuffix + ".png";
            img = loadFromClasspath(pngKey);
        }

        if (img == null) {
            // Fallback sang ảnh tĩnh mặc định (không có trạng thái)
            img = loadFromClasspath(typeName + ".png");
        }

        return img;
    }

    /** Đăng ký sprite được tạo bằng code (SpriteGenerator) */
    public void registerGenerated(String key, Image image) {
        if (image != null) {
            cache.put(key, image);
        }
    }

    private Image loadFromClasspath(String filename) {
        if (cache.containsKey(filename)) {
            return cache.get(filename);
        }

        try {
            // Thử nhiều cách load khác nhau
            var url = getClass().getResource("/sprites/" + filename);
            
            if (url == null) {
                url = Thread.currentThread().getContextClassLoader().getResource("sprites/" + filename);
            }

            if (url != null) {
                Image img = new Image(url.toExternalForm());
                if (!img.isError()) {
                    if (!debugLogged) {
                        System.out.println("[AssetManager] ✅ Loaded: " + filename 
                            + " (" + (int)img.getWidth() + "x" + (int)img.getHeight() + ")");
                    }
                    cache.put(filename, img);
                    return img;
                } else {
                    System.out.println("[AssetManager] ❌ Error loading: " + filename);
                }
            } else {
                if (!debugLogged) {
                    System.out.println("[AssetManager] ⚠ Not found: /sprites/" + filename);
                }
            }
        } catch (Exception e) {
            System.out.println("[AssetManager] ❌ Exception loading " + filename + ": " + e.getMessage());
        }
        
        cache.put(filename, null);
        return null;
    }

    /** Đánh dấu đã log xong lần đầu (tránh spam console) */
    public void markDebugComplete() {
        debugLogged = true;
    }
}
