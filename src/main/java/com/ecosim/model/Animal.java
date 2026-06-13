package com.ecosim.model;

import com.ecosim.strategy.SurvivalStrategy;
import com.ecosim.util.Constants;
import com.ecosim.util.Vector2D;

import java.util.List;
import java.util.Random;

/**
 * Lớp trừu tượng cho mọi động vật.
 * Quản lý nhu cầu sinh tồn (đói, khát, sức khỏe) và chiến lược hành vi.
 *
 * Tách biệt logic:
 * - BioLogic: hunger, thirst, health, needs → trong Animal
 * - AI Logic: quyết định hành động → delegate cho SurvivalStrategy
 * - ViewLogic: hiển thị → delegate cho Renderer (bên ngoài)
 */
public abstract class Animal extends Entity {

    // ===== Nhu cầu sinh tồn =====
    /** 0 = chết đói, MAX = no bụng */
    protected double hunger;
    /** 0 = chết khát, MAX = đã uống đủ */
    protected double thirst;
    /** 0 = chết, MAX = khỏe mạnh */
    protected double health;
    protected double maxHealth;

    // ===== Thuộc tính di chuyển =====
    /** Tốc độ di chuyển hiện tại (tiles/giây) */
    protected double speed;
    /** Tốc độ tối đa */
    protected double maxSpeed;
    /** Hướng di chuyển hiện tại */
    protected Vector2D direction;

    // ===== Thuộc tính chiến đấu =====
    /** Sức tấn công */
    protected double attackPower;
    /** Tầm nhìn (tiles) - phạm vi phát hiện entity khác */
    protected double sightRange;

    // ===== Chiến lược sinh tồn (Strategy Pattern) =====
    protected SurvivalStrategy strategy;
    /** Strategy mặc định (để quay về khi cần) */
    protected SurvivalStrategy defaultStrategy;

    // ===== Trạng thái =====
    protected AnimalState state;

    /** Thời gian ở trạng thái hiện tại */
    protected double stateTimer;

    /** Danh sách kẻ thù tự nhiên (class names) - để biết sợ ai */
    protected List<Class<? extends Animal>> naturalEnemies;

    /** Danh sách con mồi (class names) - để biết săn ai */
    protected List<Class<? extends Entity>> preyTypes;

    /** Cooldown di chuyển wandering direction */
    protected double wanderTimer;
    protected Vector2D wanderTarget;
    protected Vector2D currentTarget; // Dùng cho debug vẽ đường đi

    protected final Random random = new Random();

    protected Animal(String name, Vector2D position, int priority, double size,
                     double maxHealth, double maxSpeed, double attackPower,
                     double sightRange) {
        super(name, position, priority, size);
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.hunger = Constants.MAX_HUNGER;
        this.thirst = Constants.MAX_THIRST;
        this.maxSpeed = maxSpeed;
        this.speed = maxSpeed;
        this.attackPower = attackPower;
        this.sightRange = sightRange;
        this.direction = Vector2D.randomDirection();
        this.state = AnimalState.IDLE;
        this.stateTimer = 0;
        this.wanderTimer = 0;
        this.wanderTarget = null;
        this.currentTarget = null;
        this.naturalEnemies = List.of();
        this.preyTypes = List.of();
        //update 
        this.age = 0;
        this.reproductionCooldown = 0;
    }

    @Override
    public void update(double deltaTime, WorldMap worldMap) {
        if (!alive) return;

        // 1. Cập nhật nhu cầu sinh tồn
        updateNeeds(deltaTime);
        //update  
        age += deltaTime;

        if (reproductionCooldown > 0) {
            reproductionCooldown -= deltaTime;
        }

        // 2. Kiểm tra chết
        if (health <= 0) {
            die();
            return;
        }

        // 3. Cập nhật state timer
        stateTimer += deltaTime;
    }

    /**
     * Thực hiện hành động đã quyết định bởi strategy.
     * Gọi bởi SimulationEngine sau khi strategy.decide().
     */

    public void executeAction(Action action, double deltaTime, WorldMap worldMap) {
        if (!alive) return;

        // Lưu mục tiêu cho debug hiển thị
        if (action.getTargetPosition() != null) {
            this.currentTarget = action.getTargetPosition();
        } else if (action.getTargetEntity() != null) {
            this.currentTarget = action.getTargetEntity().getPosition();
        } else {
            this.currentTarget = null;
        }

        switch (action.getType()) {
            case IDLE -> doIdle(deltaTime);
            case WANDER -> doWander(deltaTime, worldMap);
            case MOVE_TO -> doMoveTo(
                action.getTargetPosition(),
                deltaTime,
                worldMap,
                shouldRunToward(action.getTargetEntity())
            );
            case EAT -> doEat(action.getTargetEntity(), deltaTime);
            case DRINK -> {

                                doMoveTo(
                                    action.getTargetPosition(),
                                    deltaTime,
                                    worldMap,
                                    false
                                );

                                // Nếu tới gần nước thì uống
                                if (position.distanceTo(action.getTargetPosition()) < 1.5) {
                                    drinkWater();
                                }
                            }
            case ATTACK -> doAttack(action.getTargetEntity(), deltaTime);
            case FLEE -> doFlee(action.getTargetPosition(), deltaTime, worldMap);
            case HIDE -> doMoveTo(action.getTargetPosition(), deltaTime, worldMap, true);
            case SLEEP -> doSleep(deltaTime);
        }
    }

    // ===== Cập nhật nhu cầu sinh tồn =====
    public boolean canReproduce() {

    return alive &&
           health > maxHealth * 0.5 &&
           hunger > Constants.MAX_HUNGER * 0.4 &&
           thirst > Constants.MAX_THIRST * 0.4 &&
           age > 8 &&
           reproductionCooldown <= 0;
    }   
    public void resetReproductionCooldown() {
        reproductionCooldown = Constants.REPRODUCTION_COOLDOWN;
    }
    
    /**
     * Create an offspring of this animal.
     * 
     * Note: This is now primarily used internally by AnimalReproductionFactory.
     * Each animal subclass must implement this to create offspring at nearby position.
     * 
     * @return A new offspring animal of the same species, or null if reproduction not supported
     */
    public abstract Animal createOffspring();
    /** Giảm hunger, thirst theo thời gian */
    protected void updateNeeds(double deltaTime) {
        // Đói dần
        hunger -= Constants.HUNGER_DECAY_RATE * deltaTime;
        if (hunger < 0) hunger = 0;

        // Khát dần
        thirst -= Constants.THIRST_DECAY_RATE * deltaTime;
        if (thirst < 0) thirst = 0;

        // Đói/khát → mất máu
        if (hunger <= 0) {
            health -= 10 * deltaTime;
        }
        if (thirst <= 0) {
            health -= 15 * deltaTime;
        }

        // Lưu ý: Tốc độ (speed) được quản lý bởi doMoveTo() với hệ thống gia tốc.
        // doMoveTo() đã tự tính toán giảm tốc khi đói (hunger < CRITICAL_HUNGER).
    }

    // ===== Các hành động cơ bản =====

    protected void doIdle(double deltaTime) {
        setState(AnimalState.IDLE);
    }

    protected void doWander(double deltaTime, WorldMap worldMap) {
        setState(AnimalState.WALKING);
        wanderTimer -= deltaTime;

        if (wanderTarget == null || wanderTimer <= 0 ||
            position.distanceTo(wanderTarget) < 1.0) {
            // Chọn hướng đi mới
            wanderTarget = position.add(Vector2D.random(10));
            wanderTimer = 3 + random.nextDouble() * 5; // 3-8 giây
        }

        doMoveTo(wanderTarget, deltaTime, worldMap, false);
    }

    protected void doMoveTo(Vector2D target, double deltaTime, WorldMap worldMap, boolean isRunning) {
        if (target == null) return;

        Vector2D targetDir = position.directionTo(target);
        TerrainType terrain = worldMap.getTerrainAt(position.getX(), position.getY());

        // Kiểm tra có thể đi trên terrain này không
        double terrainMod = getTerrainSpeedModifier(terrain);
        double maxAllowedSpeed = (isRunning ? maxSpeed * getRunSpeedMultiplier() : maxSpeed * 0.8) * terrainMod;
        
        // Suy giảm tốc độ khi đói
        if (hunger < Constants.CRITICAL_HUNGER) {
            maxAllowedSpeed *= 0.6;
        }

        // Tăng tốc hoặc giảm tốc dần (Acceleration/Deceleration)
        if (speed < maxAllowedSpeed) {
            speed = Math.min(maxAllowedSpeed, speed + maxAllowedSpeed * 1.5 * deltaTime);
        } else if (speed > maxAllowedSpeed) {
            speed = Math.max(maxAllowedSpeed, speed - maxAllowedSpeed * 2.5 * deltaTime);
        }

        // Lấy khoảng cách tới mục tiêu
        double distToTarget = position.distanceTo(target);
        
        // Chậm lại khi tới rất gần mục tiêu (Braking)
        if (distToTarget < 1.0) {
            speed *= distToTarget; 
        }

        double stepDistance = Math.min(speed * deltaTime, distToTarget);
        
        // Steering: Xoay mượt mà (Lerp)
        if (direction == null) direction = targetDir;
        direction = direction.lerp(targetDir, 6.0 * deltaTime).normalize();
        if (direction.magnitude() == 0) direction = targetDir;

        Vector2D newPos = position.add(direction.multiply(stepDistance));

        // Kiểm tra vị trí mới có hợp lệ không
        TerrainType newTerrain = worldMap.getTerrainAt(newPos.getX(), newPos.getY());
        if (canTraverse(newTerrain) && worldMap.isInBounds(newPos.getX(), newPos.getY())) {
            position = newPos;
            setState(isRunning ? AnimalState.RUNNING : AnimalState.WALKING);
        } else {
            // Tránh tường: Cố gắng trượt dọc theo tường
            Vector2D slidePos = position.add(new Vector2D(direction.getX(), 0).multiply(stepDistance));
            if (canTraverse(worldMap.getTerrainAt(slidePos.getX(), slidePos.getY())) && worldMap.isInBounds(slidePos.getX(), slidePos.getY())) {
                position = slidePos;
            } else {
                slidePos = position.add(new Vector2D(0, direction.getY()).multiply(stepDistance));
                if (canTraverse(worldMap.getTerrainAt(slidePos.getX(), slidePos.getY())) && worldMap.isInBounds(slidePos.getX(), slidePos.getY())) {
                    position = slidePos;
                } else {
                    wanderTarget = null;
                    wanderTimer = 0;
                    setState(AnimalState.IDLE);
                }
            }
        }
    }

    protected double getRunSpeedMultiplier() {
        return 1.3;
    }

    private boolean shouldRunToward(Entity target) {
        return target != null && isPrey(target);
    }

    protected void doEat(Entity food, double deltaTime) {
        if (food == null || !food.isAlive()) return;

        double dist = distanceTo(food);
        if (dist < 1.5) {
            setState(AnimalState.EATING);
            
            // Tốc độ lấp đầy thanh đói: 3 giây để đầy 100% (MAX_HUNGER / 3.0)
            double fillRate = Constants.MAX_HUNGER / 3.0;

            if (food instanceof Plant plant) {
                double nutrition = plant.beEaten(deltaTime); // Thực vật mất dần size
                if (this instanceof Rabbit) {
                    hunger = Math.min(Constants.MAX_HUNGER, hunger + fillRate * deltaTime);
                } else {
                    hunger = Math.min(Constants.MAX_HUNGER, hunger + nutrition);
                }
                health = Math.min(maxHealth, health + 5 * deltaTime);
            } else if (food instanceof Animal prey) {
                // Ăn thịt con mồi đã chết
                if (!prey.isAlive()) {
                    hunger = Math.min(Constants.MAX_HUNGER, hunger + fillRate * deltaTime);
                }
            }
        }
    }

    protected void doAttack(Entity target, double deltaTime) {
        if (target == null || !target.isAlive()) return;

        double dist = distanceTo(target);
        if (dist < 1.5) {
            setState(AnimalState.ATTACKING);
            if (target instanceof Animal prey) {
                prey.takeDamage(attackPower * deltaTime);
            }
        } else {
            // Đuổi theo con mồi
            setState(AnimalState.RUNNING);
        }
    }

    protected void doFlee(Vector2D threatPos, double deltaTime, WorldMap worldMap) {
        if (threatPos == null) return;
        setState(AnimalState.FLEEING);

        // Chạy ngược hướng với kẻ thù
        Vector2D awayDir = threatPos.directionTo(position);
        Vector2D fleeTarget = position.add(awayDir.multiply(5));
        doMoveTo(fleeTarget, deltaTime, worldMap, true);
    }

    protected void doSleep(double deltaTime) {
        setState(AnimalState.SLEEPING);
        // Ngủ hồi máu
        health = Math.min(maxHealth, health + 5 * deltaTime);
    }

    // ===== Tiện ích =====

    /** Nhận sát thương */
    public void takeDamage(double damage) {
        health -= damage;
        if (health <= 0) {
            health = 0;
            die();
        }
    }
    //update
    protected double age;
    protected double reproductionCooldown;
    //update
    /** Chết */
    protected void die() {
        alive = false;
        state = AnimalState.DEAD;
    }

    /** Uống nước - gọi khi đứng gần nguồn nước */
    public void drinkWater() {
        setState(AnimalState.DRINKING);
        thirst = Math.min(Constants.MAX_THIRST, thirst + 30);
    }

    /** Đổi state và reset timer */
    protected void setState(AnimalState newState) {
        if (this.state != newState) {
            this.state = newState;
            this.stateTimer = 0;
        }
    }

    /** Kiểm tra entity có phải là kẻ thù tự nhiên */
    public boolean isEnemy(Entity entity) {
        if (entity == null || !entity.isAlive()) return false;
        return naturalEnemies.stream().anyMatch(c -> c.isInstance(entity));
    }

    /** Kiểm tra entity có phải con mồi */
    public boolean isPrey(Entity entity) {
        if (entity == null || !entity.isAlive()) return false;
        return preyTypes.stream().anyMatch(c -> c.isInstance(entity));
    }

    /** Đổi chiến lược sinh tồn (runtime) */
    public void setStrategy(SurvivalStrategy strategy) {
        this.strategy = strategy;
    }

    /** Quay về strategy mặc định */
    public void resetStrategy() {
        this.strategy = defaultStrategy;
    }

    // ===== Getters =====
    public double getHunger() { return hunger; }
    public double getThirst() { return thirst; }
    public double getHealth() { return health; }
    public double getMaxHealth() { return maxHealth; }
    public double getSpeed() { return speed; }
    public double getMaxSpeed() { return maxSpeed; }
    public double getAttackPower() { return attackPower; }
    public double getSightRange() { return sightRange; }
    public AnimalState getState() { return state; }
    public SurvivalStrategy getStrategy() { return strategy; }
    public Vector2D getDirection() { return direction; }
    public List<Class<? extends Animal>> getNaturalEnemies() { return naturalEnemies; }
    public List<Class<? extends Entity>> getPreyTypes() { return preyTypes; }

    public void setHunger(double hunger) { this.hunger = Math.max(0, Math.min(Constants.MAX_HUNGER, hunger)); }
    public void setThirst(double thirst) { this.thirst = Math.max(0, Math.min(Constants.MAX_THIRST, thirst)); }
    
    public double getAge() { return age; }
    public double getReproductionCooldown() { return reproductionCooldown; }
    public Vector2D getTargetPosition() { return currentTarget; }
}
