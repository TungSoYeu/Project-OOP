package com.ecosim;

import com.ecosim.engine.SimulationEngine;

public class TestEngine {
    public static void main(String[] args) {
        try {
            System.out.println("Init engine...");
            SimulationEngine engine = new SimulationEngine();
            engine.start();
            System.out.println("Running ticks...");
            for (int i = 0; i < 100; i++) {
                engine.tick(0.016);
            }
            System.out.println("Done without errors.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
