package com.ecosim;

import com.ecosim.engine.SimulationEngine;
import com.ecosim.util.Constants;
import com.ecosim.view.GameView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point cho ứng dụng Wild-Life Eco Simulation.
 *
 * Kiến trúc MVC + Strategy Pattern:
 * - Model: com.ecosim.model (Entity hierarchy, Terrain, Season)
 * - View: com.ecosim.view (GameView, Renderer, Camera)
 * - Controller: com.ecosim.engine (SimulationEngine, EntityManager)
 * - Strategy: com.ecosim.strategy (SurvivalStrategy implementations)
 */
public class App extends Application {

    private GameView gameView;

    @Override
    public void start(Stage stage) {
        // Khởi tạo engine (BioLogic)
        SimulationEngine engine = new SimulationEngine();

        // Khởi tạo view (ViewLogic)
        gameView = new GameView(engine);

        // Tạo scene
        Scene scene = new Scene(gameView, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

        // Load CSS
        var cssUrl = getClass().getResource("/css/style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        // Cấu hình stage
        stage.setTitle(Constants.WINDOW_TITLE);
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);

        // Dọn dẹp khi đóng
        stage.setOnCloseRequest(e -> {
            if (gameView != null) {
                gameView.stopGameLoop();
            }
        });

        stage.show();

        System.out.println("===========================================");
        System.out.println("  🌿 Wild-Life Eco Simulation v1.0");
        System.out.println("  Hệ thống Mô phỏng Hệ sinh thái Hoang dã");
        System.out.println("===========================================");
        System.out.println("▶ Nhấn 'Bắt đầu' để chạy mô phỏng");
        System.out.println("🖱 Click trái để chọn entity");
        System.out.println("🖱 Click phải để mở menu tạo entity");
        System.out.println("🖱 Kéo chuột trái/giữa để di chuyển camera");
        System.out.println("🖱 Scroll để zoom in/out");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
