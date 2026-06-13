package com.ecosim.model;

import com.ecosim.util.Constants;
import com.ecosim.util.Vector2D;

/**
 * Factory for creating animal offspring.
 * 
 * Implements Strategy Pattern to handle species-specific reproduction logic.
 * Replaces abstract createOffspring() method for better scalability and SOLID compliance.
 * 
 * Benefits:
 * - Separates reproduction logic from animal classes
 * - Easier to add/modify reproduction rules per species
 * - Some species (Hunter) can have null/disabled reproduction
 * - Centralizes offspring validation (bounds checking, population limits)
 * 
 * Usage:
 *   Animal baby = AnimalReproductionFactory.createOffspring(parent, worldMap);
 */
public final class AnimalReproductionFactory {
    
    private AnimalReproductionFactory() {
        // Utility class - no instantiation
    }

    /**
     * Create an offspring for the given animal parent.
     * 
     * @param parent The animal parent
     * @param worldMap The world map for bounds validation
     * @return A new animal offspring, or null if reproduction is not supported or fails
     */
    public static Animal createOffspring(Animal parent, WorldMap worldMap) {
        if (parent == null || worldMap == null) {
            return null;
        }

        // Route to species-specific reproduction
        Animal offspring = createSpeciesOffspring(parent);
        
        if (offspring == null) {
            return null;
        }

        // Validate and adjust spawn position to be within bounds
        Vector2D safePosition = findValidSpawnPosition(offspring.getPosition(), worldMap);
        if (safePosition == null) {
            // Could not find valid spawn position after retries
            return null;
        }
        
        offspring.setPosition(safePosition);
        return offspring;
    }

    /**
     * Create species-specific offspring.
     * Each animal type has its own reproduction rules.
     * 
     * @param parent The parent animal
     * @return A new offspring of the same species, or null if not reproducible
     */
    private static Animal createSpeciesOffspring(Animal parent) {
        return switch (parent.getClass().getSimpleName()) {
            case "Rabbit" -> createRabbitOffspring((Rabbit) parent);
            case "Deer" -> createDeerOffspring((Deer) parent);
            case "Wolf" -> createWolfOffspring((Wolf) parent);
            case "Tiger" -> createTigerOffspring((Tiger) parent);
            case "Elephant" -> createElephantOffspring((Elephant) parent);
            case "Hunter" -> null; // Hunters don't reproduce
            default -> null; // Unknown species
        };
    }

    private static Animal createRabbitOffspring(Rabbit parent) {
        Vector2D offset = Vector2D.random(Constants.OFFSPRING_MAX_DISTANCE);
        return new Rabbit(parent.getPosition().add(offset));
    }

    private static Animal createDeerOffspring(Deer parent) {
        Vector2D offset = Vector2D.random(Constants.OFFSPRING_MAX_DISTANCE);
        return new Deer(parent.getPosition().add(offset));
    }

    private static Animal createWolfOffspring(Wolf parent) {
        Vector2D offset = Vector2D.random(Constants.OFFSPRING_MAX_DISTANCE);
        return new Wolf(parent.getPosition().add(offset));
    }

    private static Animal createTigerOffspring(Tiger parent) {
        Vector2D offset = Vector2D.random(Constants.OFFSPRING_MAX_DISTANCE);
        return new Tiger(parent.getPosition().add(offset));
    }

    private static Animal createElephantOffspring(Elephant parent) {
        Vector2D offset = Vector2D.random(Constants.OFFSPRING_MAX_DISTANCE);
        return new Elephant(parent.getPosition().add(offset));
    }

    /**
     * Find a valid spawn position within map bounds.
     * Attempts multiple times with random offsets if the initial position is out of bounds.
     * 
     * @param preferredPosition The desired spawn position
     * @param worldMap The world map for bounds validation
     * @return A valid position within bounds, or null if no valid position found after retries
     */
    private static Vector2D findValidSpawnPosition(Vector2D preferredPosition, WorldMap worldMap) {
        // Check if preferred position is already valid
        if (worldMap.isInBounds(preferredPosition.getX(), preferredPosition.getY())) {
            return preferredPosition;
        }

        // Retry with new random offsets
        for (int attempt = 0; attempt < Constants.OFFSPRING_SPAWN_RETRIES; attempt++) {
            Vector2D randomOffset = Vector2D.random(Constants.OFFSPRING_MAX_DISTANCE);
            Vector2D candidatePos = preferredPosition.add(randomOffset);
            
            if (worldMap.isInBounds(candidatePos.getX(), candidatePos.getY())) {
                return candidatePos;
            }
        }

        // Last resort: clamp position to map bounds (with margin 1.5)
        double margin = 1.5;
        double x = Math.max(margin, Math.min(worldMap.getWidth() - margin, preferredPosition.getX()));
        double y = Math.max(margin, Math.min(worldMap.getHeight() - margin, preferredPosition.getY()));
        return new Vector2D(x, y);
    }
}
