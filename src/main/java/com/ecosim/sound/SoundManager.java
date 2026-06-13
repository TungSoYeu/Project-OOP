package com.ecosim.sound;

import javafx.scene.media.AudioClip;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Quản lý âm thanh cho simulation.
 * Phát âm thanh dựa trên sự kiện (hổ gầm, chim hót, ...).
 *
 * Sử dụng JavaFX AudioClip cho âm thanh ngắn.
 * Các file .wav được đặt trong resources/sounds/.
 *
 * LƯU Ý: Nếu file âm thanh không tồn tại, SoundManager sẽ bỏ qua
 * mà không gây lỗi (graceful degradation).
 */
public class SoundManager {
    private final Map<String, AudioClip> clips;
    private boolean enabled;
    private double volume;
    private final Map<String, Long> lastPlayed;

    /** Danh sách sound event IDs */
    public static final String BIRD_CHIRP = "bird_chirp";
    public static final String TIGER_ROAR = "tiger_roar";
    public static final String FOOTSTEP_LEAVES = "footstep_leaves";
    public static final String WATER_SPLASH = "water_splash";
    public static final String WOLF_HOWL = "wolf_howl";
    public static final String EAT_SOUND = "eat_sound";

    public SoundManager() {
        this.clips = new HashMap<>();
        this.lastPlayed = new HashMap<>();
        this.enabled = true;
        this.volume = 0.5;
        loadSounds();
    }

    /** Tải tất cả file âm thanh */
    private void loadSounds() {
        loadClip(BIRD_CHIRP, "/sounds/bird_chirp.wav");
        loadClip(TIGER_ROAR, "/sounds/tiger_roar.wav");
        loadClip(FOOTSTEP_LEAVES, "/sounds/footstep_leaves.wav");
        loadClip(WATER_SPLASH, "/sounds/water_splash.wav");
        loadClip(WOLF_HOWL, "/sounds/wolf_howl.wav");
        loadClip(EAT_SOUND, "/sounds/eat_sound.wav");
    }

    private void loadClip(String id, String resourcePath) {
        try {
            URL url = getClass().getResource(resourcePath);
            if (url != null) {
                AudioClip clip = new AudioClip(url.toExternalForm());
                clips.put(id, clip);
            }
            // Nếu file không tồn tại → bỏ qua
        } catch (Exception e) {
            // Graceful degradation — tiếp tục chạy mà không có sound
            System.out.println("[SoundManager] Không thể tải: " + resourcePath);
        }
    }

    /** Phát âm thanh */
    public void play(String soundId) {
        play(soundId, volume);
    }

    /** Phát âm thanh với volume tùy chỉnh */
    public void play(String soundId, double customVolume) {
        if (!enabled) return;
        
        long now = System.currentTimeMillis();
        // Cooldown 300ms per sound to prevent lag and terminal spam
        if (now - lastPlayed.getOrDefault(soundId, 0L) < 300) {
            return;
        }

        AudioClip clip = clips.get(soundId);
        if (clip != null) {
            try {
                clip.play(customVolume);
                lastPlayed.put(soundId, now);
            } catch (Exception e) {
                // Ignore internal JavaFX media exceptions
            }
        }
    }

    /** Bật/tắt âm thanh */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() { return enabled; }

    public void setVolume(double volume) {
        this.volume = Math.max(0, Math.min(1, volume));
    }

    public double getVolume() { return volume; }
}
