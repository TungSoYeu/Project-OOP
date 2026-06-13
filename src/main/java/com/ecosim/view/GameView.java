package com.ecosim.view;

import com.ecosim.engine.SimulationEngine;
import com.ecosim.model.*;
import com.ecosim.util.Constants;
import com.ecosim.sound.SoundManager;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.Map;

/**
 * Main game view - chứa Canvas rendering, Toolbar, và InfoPanel.
 * Tách biệt hoàn toàn ViewLogic khỏi BioLogic.
 */
public class GameView extends BorderPane {

    private final SimulationEngine engine;
    private final Camera camera;
    private Renderer renderer;
    private final Canvas canvas;
    private final GraphicsContext gc;
    private AnimationTimer gameLoop;
    private final SoundManager soundManager;

    // UI Components
    private final VBox sidebar;
    private final HBox toolbar;
    private final Label statusLabel;
    private final Label seasonLabel;
    private final Label entityCountLabel;
    private final Label fpsLabel;

    // Interaction state
    private Entity selectedEntity;
    private double lastMouseX, lastMouseY;
    private String currentTool = "select"; // "select", "grass", "rock", "spawn"

    // FPS tracking
    private long lastFrameTime;
    private int frameCount;
    private double fpsTimer;
    private int currentFps;

    // Context menu
    private ContextMenu contextMenu;

    public GameView(SimulationEngine engine) {
        this.engine = engine;
        this.camera = new Camera(900, 600);
        this.renderer = new BasicRenderer();
        this.soundManager = new SoundManager();

        // Canvas setup
        this.canvas = new Canvas(900, 600);
        this.gc = canvas.getGraphicsContext2D();

        // Layout
        this.toolbar = createToolbar();
        this.sidebar = createSidebar();
        this.statusLabel = new Label("⏸ Tạm dừng");
        this.seasonLabel = new Label();
        this.entityCountLabel = new Label();
        this.fpsLabel = new Label("FPS: 0");

        // Build UI
        buildUI();

        // Setup input
        setupInput();

        // Context menu
        createContextMenu();

        // Camera mặc định: xem toàn bản đồ
        camera.viewFullMap();

        // Start game loop
        startGameLoop();
    }

    private void buildUI() {
        // Top: Toolbar
        setTop(toolbar);

        // Center: Canvas
        StackPane canvasHolder = new StackPane(canvas);
        canvasHolder.setStyle("-fx-background-color: #1a1a2e;");

        // Canvas auto-resize
        canvas.widthProperty().bind(canvasHolder.widthProperty());
        canvas.heightProperty().bind(canvasHolder.heightProperty());
        canvas.widthProperty().addListener((obs, o, n) -> camera.setViewportSize(n.doubleValue(), canvas.getHeight()));
        canvas.heightProperty().addListener((obs, o, n) -> camera.setViewportSize(canvas.getWidth(), n.doubleValue()));

        setCenter(canvasHolder);

        // Right: Sidebar
        setRight(sidebar);

        // Bottom: Status bar
        HBox statusBar = createStatusBar();
        setBottom(statusBar);
    }

    // ===== Toolbar =====
    private HBox createToolbar() {
        HBox bar = new HBox(10);
        bar.setPadding(new Insets(8, 15, 8, 15));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("toolbar");

        // Play/Pause
        Button playBtn = new Button("▶ Bắt đầu");
        playBtn.getStyleClass().add("btn-primary");
        playBtn.setOnAction(e -> {
            engine.toggle();
            playBtn.setText(engine.isRunning() ? "⏸ Tạm dừng" : "▶ Bắt đầu");
            statusLabel.setText(engine.isRunning() ? "▶ Đang chạy" : "⏸ Tạm dừng");
        });

        // Speed controls
        Label speedLabel = new Label("Tốc độ:");
        speedLabel.setTextFill(Color.WHITE);
        ComboBox<String> speedBox = new ComboBox<>();
        speedBox.getItems().addAll("0.5x", "1x", "2x", "4x", "8x");
        speedBox.setValue("1x");
        speedBox.setOnAction(e -> {
            String val = speedBox.getValue().replace("x", "");
            engine.setSpeedMultiplier(Double.parseDouble(val));
        });

        // Render mode toggle
        Button modeBtn = new Button("🎨 " + renderer.getModeName());
        modeBtn.setOnAction(e -> {
            if (renderer instanceof BasicRenderer) {
                renderer = new SpriteRenderer();
            } else {
                renderer = new BasicRenderer();
            }
            modeBtn.setText("🎨 " + renderer.getModeName());
        });

        // View presets
        Label viewLabel = new Label("Xem vùng:");
        viewLabel.setTextFill(Color.WHITE);
        ComboBox<String> viewBox = new ComboBox<>();
        viewBox.getItems().addAll("🗺 Toàn bản đồ", "🌾 Đồng cỏ", "🌲 Rừng rậm", "💧 Hồ nước");
        viewBox.setValue("🗺 Toàn bản đồ");
        viewBox.setOnAction(e -> {
            switch (viewBox.getValue()) {
                case "🗺 Toàn bản đồ" -> camera.viewFullMap();
                case "🌾 Đồng cỏ" -> camera.viewGrassland();
                case "🌲 Rừng rậm" -> camera.viewForest();
                case "💧 Hồ nước" -> camera.viewLake();
            }
        });

        // Tool selection
        Label toolLabel = new Label("Công cụ:");
        toolLabel.setTextFill(Color.WHITE);
        ToggleGroup toolGroup = new ToggleGroup();
        RadioButton selectTool = new RadioButton("🔍 Chọn");
        selectTool.setToggleGroup(toolGroup);
        selectTool.setSelected(true);
        selectTool.setTextFill(Color.WHITE);
        selectTool.setOnAction(e -> currentTool = "select");

        RadioButton grassTool = new RadioButton("🌱 Gieo cỏ");
        grassTool.setToggleGroup(toolGroup);
        grassTool.setTextFill(Color.WHITE);
        grassTool.setOnAction(e -> currentTool = "grass");

        RadioButton rockTool = new RadioButton("🪨 Đặt đá");
        rockTool.setToggleGroup(toolGroup);
        rockTool.setTextFill(Color.WHITE);
        rockTool.setOnAction(e -> currentTool = "rock");

        Separator sep1 = new Separator();
        sep1.setOrientation(javafx.geometry.Orientation.VERTICAL);
        Separator sep2 = new Separator();
        sep2.setOrientation(javafx.geometry.Orientation.VERTICAL);
        Separator sep3 = new Separator();
        sep3.setOrientation(javafx.geometry.Orientation.VERTICAL);

        bar.getChildren().addAll(
            playBtn, sep1,
            speedLabel, speedBox, modeBtn, sep2,
            viewLabel, viewBox, sep3,
            toolLabel, selectTool, grassTool, rockTool
        );

        return bar;
    }

    // ===== Sidebar =====
    private VBox createSidebar() {
        VBox side = new VBox(10);
        side.setPadding(new Insets(10));
        side.setPrefWidth(220);
        side.getStyleClass().add("sidebar");

        // Title
        Label title = new Label("📊 Thống kê");
        title.setFont(Font.font("System", FontWeight.BOLD, 14));
        title.setTextFill(Color.WHITE);

        side.getChildren().add(title);
        return side;
    }

    /** Cập nhật sidebar với thông tin thống kê */
    private void updateSidebar() {
        sidebar.getChildren().clear();

        // Title
        Label title = new Label("📊 Thống kê");
        title.setFont(Font.font("System", FontWeight.BOLD, 14));
        title.setTextFill(Color.WHITE);
        sidebar.getChildren().add(title);

        // Season info
        var season = engine.getSeasonManager().getCurrentSeason();
        Label seasonInfo = new Label(season.getIcon() + " " + season.getDisplayName()
            + " (Năm " + engine.getSeasonManager().getYearCount() + ")");
        seasonInfo.setTextFill(season.getThemeColor());
        seasonInfo.setFont(Font.font("System", FontWeight.BOLD, 13));

        // Season progress bar
        ProgressBar seasonProgress = new ProgressBar(engine.getSeasonManager().getSeasonProgress());
        seasonProgress.setPrefWidth(200);
        seasonProgress.setMaxHeight(6);

        sidebar.getChildren().addAll(seasonInfo, seasonProgress, new Separator());

        // Entity counts
        Label countTitle = new Label("🐾 Số lượng thực thể");
        countTitle.setTextFill(Color.LIGHTGRAY);
        countTitle.setFont(Font.font("System", FontWeight.BOLD, 12));
        sidebar.getChildren().add(countTitle);

        Map<String, Long> counts = engine.getEntityManager().getEntityCountByType();
        for (var entry : counts.entrySet()) {
            String icon = getEntityIcon(entry.getKey());
            Label countLabel = new Label(icon + " " + entry.getKey() + ": " + entry.getValue());
            countLabel.setTextFill(Color.LIGHTGRAY);
            sidebar.getChildren().add(countLabel);
        }

        sidebar.getChildren().add(new Separator());

        // Selected entity info
        if (selectedEntity != null && selectedEntity.isAlive()) {
            Label selectedTitle = new Label("🔍 " + selectedEntity.getTypeName() + " đã chọn");
            selectedTitle.setFont(Font.font("System", FontWeight.BOLD, 12));
            selectedTitle.setTextFill(Color.LIGHTYELLOW);
            sidebar.getChildren().add(selectedTitle);

            if (selectedEntity instanceof Animal animal) {
                addInfoBar(sidebar, "❤ HP", animal.getHealth(), animal.getMaxHealth(), Color.RED);
                addInfoBar(sidebar, "🍖 Đói", animal.getHunger(), Constants.MAX_HUNGER, Color.GOLD);
                addInfoBar(sidebar, "💧 Khát", animal.getThirst(), Constants.MAX_THIRST, Color.DODGERBLUE);

                Label stateLabel = new Label("Trạng thái: " + animal.getState().getDisplayName());
                stateLabel.setTextFill(Color.LIGHTGRAY);
                sidebar.getChildren().add(stateLabel);

                if (animal.getStrategy() != null) {
                    Label stratLabel = new Label("Chiến lược: " + animal.getStrategy().getName());
                    stratLabel.setTextFill(Color.LIGHTCYAN);
                    sidebar.getChildren().add(stratLabel);
                }

                Label posLabel = new Label("Vị trí: " + animal.getPosition());
                posLabel.setTextFill(Color.GRAY);
                sidebar.getChildren().add(posLabel);
            }
        }

        // Zoom info
        sidebar.getChildren().add(new Separator());
        Label zoomLabel = new Label(String.format("🔎 Zoom: %.0f%%", camera.getZoom() * 100));
        zoomLabel.setTextFill(Color.GRAY);
        sidebar.getChildren().add(zoomLabel);

        Label fpsInfo = new Label("FPS: " + currentFps);
        fpsInfo.setTextFill(Color.GRAY);
        sidebar.getChildren().add(fpsInfo);
    }

    private void addInfoBar(VBox container, String label, double value, double max, Color color) {
        Label lbl = new Label(String.format("%s: %.0f/%.0f", label, value, max));
        lbl.setTextFill(Color.LIGHTGRAY);
        ProgressBar bar = new ProgressBar(value / max);
        bar.setPrefWidth(190);
        bar.setMaxHeight(8);
        // Tạo style với màu tùy chỉnh
        String colorHex = String.format("#%02X%02X%02X",
            (int)(color.getRed()*255), (int)(color.getGreen()*255), (int)(color.getBlue()*255));
        bar.setStyle("-fx-accent: " + colorHex + ";");
        container.getChildren().addAll(lbl, bar);
    }

    // ===== Status Bar =====
    private HBox createStatusBar() {
        HBox bar = new HBox(20);
        bar.setPadding(new Insets(5, 15, 5, 15));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("status-bar");

        statusLabel.setTextFill(Color.LIGHTGRAY);
        seasonLabel.setTextFill(Color.LIGHTGRAY);
        entityCountLabel.setTextFill(Color.LIGHTGRAY);
        fpsLabel.setTextFill(Color.GRAY);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bar.getChildren().addAll(statusLabel, seasonLabel, entityCountLabel, spacer, fpsLabel);
        return bar;
    }

    // ===== Context Menu =====
    private void createContextMenu() {
        contextMenu = new ContextMenu();

        MenuItem spawnRabbit = new MenuItem("🐰 Thêm Thỏ");
        spawnRabbit.setOnAction(e -> spawnAtContextPos("Thỏ"));

        MenuItem spawnDeer = new MenuItem("🦌 Thêm Hươu");
        spawnDeer.setOnAction(e -> spawnAtContextPos("Hươu"));

        MenuItem spawnWolf = new MenuItem("🐺 Thêm Sói");
        spawnWolf.setOnAction(e -> spawnAtContextPos("Sói"));

        MenuItem spawnTiger = new MenuItem("🐅 Thêm Hổ");
        spawnTiger.setOnAction(e -> spawnAtContextPos("Hổ"));

        MenuItem spawnElephant = new MenuItem("🐘 Thêm Voi");
        spawnElephant.setOnAction(e -> spawnAtContextPos("Voi"));

        MenuItem spawnHunter = new MenuItem("🏹 Thêm Thợ săn");
        spawnHunter.setOnAction(e -> spawnAtContextPos("Thợ săn"));

        SeparatorMenuItem sep = new SeparatorMenuItem();

        MenuItem plantGrass = new MenuItem("🌱 Gieo cỏ");
        plantGrass.setOnAction(e -> {
            double wx = camera.screenToWorldX(lastMouseX);
            double wy = camera.screenToWorldY(lastMouseY);
            engine.plantGrass(wx, wy);
        });

        MenuItem placeRock = new MenuItem("🪨 Đặt vách đá");
        placeRock.setOnAction(e -> {
            double wx = camera.screenToWorldX(lastMouseX);
            double wy = camera.screenToWorldY(lastMouseY);
            engine.placeRock((int) wx, (int) wy);
        });

        contextMenu.getItems().addAll(
            spawnRabbit, spawnDeer, spawnWolf, spawnTiger, spawnElephant, spawnHunter,
            sep, plantGrass, placeRock
        );
    }

    private double contextWorldX, contextWorldY;

    private void spawnAtContextPos(String type) {
        engine.spawnEntity(type, contextWorldX, contextWorldY);
    }

    // ===== Input Handling =====
    private void setupInput() {
        // Mouse click
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseReleased(this::handleMouseReleased);

        // Scroll zoom
        canvas.setOnScroll(this::handleScroll);
    }

    private void handleMousePressed(MouseEvent e) {
        lastMouseX = e.getX();
        lastMouseY = e.getY();

        if (e.getButton() == MouseButton.SECONDARY) {
            // Right click: context menu
            contextWorldX = camera.screenToWorldX(e.getX());
            contextWorldY = camera.screenToWorldY(e.getY());
            contextMenu.show(canvas, e.getScreenX(), e.getScreenY());
            return;
        }

        contextMenu.hide();

        if (e.getButton() == MouseButton.PRIMARY) {
            double wx = camera.screenToWorldX(e.getX());
            double wy = camera.screenToWorldY(e.getY());

            switch (currentTool) {
                case "select" -> {
                    // Tìm entity gần nhất để chọn
                    selectedEntity = findEntityAt(wx, wy);
                }
                case "grass" -> engine.plantGrass(wx, wy);
                case "rock" -> engine.placeRock((int) wx, (int) wy);
            }
        }
    }

    private void handleMouseDragged(MouseEvent e) {
        double deltaX = e.getX() - lastMouseX;
        double deltaY = e.getY() - lastMouseY;
        boolean primaryDown = e.isPrimaryButtonDown();
        boolean middleDown = e.isMiddleButtonDown();

        if (middleDown || (primaryDown && currentTool.equals("select"))) {
            camera.pan(deltaX, deltaY);
        }

        // Vẽ liên tục khi kéo với tool cỏ/đá
        if (primaryDown) {
            double wx = camera.screenToWorldX(e.getX());
            double wy = camera.screenToWorldY(e.getY());
            if (currentTool.equals("grass")) engine.plantGrass(wx, wy);
            if (currentTool.equals("rock")) engine.placeRock((int) wx, (int) wy);
        }

        lastMouseX = e.getX();
        lastMouseY = e.getY();
    }

    private void handleMouseReleased(MouseEvent e) {
    }

    private void handleScroll(ScrollEvent e) {
        double factor = e.getDeltaY() > 0 ? 1.15 : 0.87;
        camera.zoomAt(e.getX(), e.getY(), factor);
    }

    /** Tìm entity gần vị trí world click nhất */
    private Entity findEntityAt(double worldX, double worldY) {
        Entity closest = null;
        double minDist = 2.0; // max click distance in tiles

        for (Entity entity : engine.getEntityManager().getEntities()) {
            if (!entity.isAlive()) continue;
            double dist = entity.getPosition().distanceTo(new com.ecosim.util.Vector2D(worldX, worldY));
            if (dist < minDist) {
                minDist = dist;
                closest = entity;
            }
        }
        return closest;
    }

    // ===== Game Loop =====
    private void startGameLoop() {
        lastFrameTime = System.nanoTime();

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double deltaTime = (now - lastFrameTime) / 1_000_000_000.0;
                lastFrameTime = now;

                // Giới hạn delta (tránh lag spike)
                deltaTime = Math.min(deltaTime, 0.05);

                // Update simulation
                engine.tick(deltaTime);

                // Render
                render();

                // Sound integration
                processSounds();

                // FPS counter
                frameCount++;
                fpsTimer += deltaTime;
                if (fpsTimer >= 1.0) {
                    currentFps = frameCount;
                    frameCount = 0;
                    fpsTimer = 0;

                    // Cập nhật sidebar mỗi giây (tối ưu performance)
                    updateSidebar();
                    updateStatusBar();
                }
            }
        };
        gameLoop.start();
    }

    /** Render toàn bộ một frame */
    private void render() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        // Clear
        gc.setFill(Color.web("#1a1a2e"));
        gc.fillRect(0, 0, w, h);

        // Vẽ terrain
        renderer.renderTerrain(gc, engine.getWorldMap(), camera);

        // Vẽ entities
        List<Entity> entities = engine.getEntityManager().getEntities();
        renderer.renderEntities(gc, entities, camera);

        // Vẽ info cho entity đã chọn
        if (selectedEntity != null && selectedEntity.isAlive()) {
            renderer.renderEntityInfo(gc, selectedEntity, camera);

            // Vẽ viền highlight
            double sx = camera.worldToScreenX(selectedEntity.getPosition().getX());
            double sy = camera.worldToScreenY(selectedEntity.getPosition().getY());
            double size = camera.entityScreenSize(selectedEntity.getSize()) + 6;
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(2);
            gc.setLineDashes(4);
            gc.strokeOval(sx - size / 2, sy - size / 2, size, size);
            gc.setLineDashes(null);
        }

        // Vẽ mini grid khi zoom thấp
        if (camera.getZoom() < 0.5) {
            drawMiniGrid(gc);
        }

        // Vẽ tool cursor
        drawToolCursor(gc);
    }

    private void drawMiniGrid(GraphicsContext gc) {
        gc.setStroke(Color.rgb(255, 255, 255, 0.1));
        gc.setLineWidth(0.5);

        // Vẽ đường viền vùng
        drawRegionBorder(gc, Constants.GRASSLAND_X1, Constants.GRASSLAND_Y1,
                         Constants.GRASSLAND_X2, Constants.GRASSLAND_Y2, "Đồng cỏ", Color.LIMEGREEN);
        drawRegionBorder(gc, Constants.FOREST_X1, Constants.FOREST_Y1,
                         Constants.FOREST_X2, Constants.FOREST_Y2, "Rừng rậm", Color.DARKGREEN);
        drawRegionBorder(gc, Constants.LAKE_X1, Constants.LAKE_Y1,
                         Constants.LAKE_X2, Constants.LAKE_Y2, "Hồ nước", Color.DODGERBLUE);
    }

    private void drawRegionBorder(GraphicsContext gc, int x1, int y1, int x2, int y2, String name, Color color) {
        double sx1 = camera.worldToScreenX(x1);
        double sy1 = camera.worldToScreenY(y1);
        double sx2 = camera.worldToScreenX(x2);
        double sy2 = camera.worldToScreenY(y2);

        gc.setStroke(color.deriveColor(0, 1, 1, 0.5));
        gc.setLineWidth(2);
        gc.setLineDashes(6);
        gc.strokeRect(sx1, sy1, sx2 - sx1, sy2 - sy1);
        gc.setLineDashes(null);

        gc.setFill(color);
        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.fillText(name, sx1 + 5, sy1 + 15);
    }

    private void drawToolCursor(GraphicsContext gc) {
        // Hiển thị tool indicator ở góc
        String toolText = switch (currentTool) {
            case "grass" -> "🌱 Gieo cỏ (Click/Kéo)";
            case "rock" -> "🪨 Đặt vách đá (Click/Kéo)";
            default -> "";
        };
        if (!toolText.isEmpty()) {
            gc.setFill(Color.rgb(0, 0, 0, 0.6));
            gc.fillRoundRect(10, canvas.getHeight() - 35, 200, 25, 5, 5);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("System", 12));
            gc.fillText(toolText, 18, canvas.getHeight() - 18);
        }
    }

    private void updateStatusBar() {
        var season = engine.getSeasonManager().getCurrentSeason();
        seasonLabel.setText(season.getIcon() + " " + season.getDisplayName());
        entityCountLabel.setText("🐾 " + engine.getEntityManager().getAnimalCount()
            + " con vật | 🌿 " + engine.getEntityManager().getPlantCount() + " thực vật");
        fpsLabel.setText("FPS: " + currentFps);
    }

    private String getEntityIcon(String typeName) {
        return switch (typeName) {
            case "Cỏ" -> "🌿";
            case "Cây ăn quả" -> "🌳";
            case "Thỏ" -> "🐰";
            case "Hươu" -> "🦌";
            case "Sói" -> "🐺";
            case "Hổ" -> "🐅";
            case "Thợ săn" -> "🏹";
            case "Voi" -> "🐘";
            default -> "•";
        };
    }

    public void stopGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    private void processSounds() {
        if (engine.getSpeedMultiplier() > 2.0) return; // Không phát sound khi tua nhanh
        for (Entity e : engine.getEntityManager().getEntities()) {
            if (!e.isAlive()) continue;

            if (e instanceof Tiger tiger && tiger.isRoaring()) {
                soundManager.play(SoundManager.TIGER_ROAR);
            }
            if (e instanceof Wolf && Math.random() < 0.001) { // Thi thoảng sói hú
                soundManager.play(SoundManager.WOLF_HOWL, 0.3);
            }
            if (e instanceof Animal animal) {
                if (animal.getState() == AnimalState.EATING && Math.random() < 0.005) {
                    soundManager.play(SoundManager.EAT_SOUND, 0.4);
                }
                if (animal.getState() == AnimalState.DRINKING && Math.random() < 0.005) {
                    soundManager.play(SoundManager.WATER_SPLASH, 0.4);
                }
                if (animal.getState() == AnimalState.RUNNING && Math.random() < 0.002) {
                    soundManager.play(SoundManager.FOOTSTEP_LEAVES, 0.2);
                }
            }
        }
        
        // Thi thoảng chim hót
        if (Math.random() < 0.002) {
            soundManager.play(SoundManager.BIRD_CHIRP, 0.3);
        }
    }
}
