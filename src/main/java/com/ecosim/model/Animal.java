package com.ecosim.model;

import com.ecosim.strategy.SurvivalStrategy;
import com.ecosim.util.Constants;
import com.ecosim.util.Vector2D;

import java.util.ArrayList;
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
    private static final double DAMAGE_EVENT_INTERVAL = 0.35;
    private static final double MIN_INTERACTION_REACH = 1.5;
    private static final double INTERACTION_MARGIN = 0.2;
    private static final int WANDER_TARGET_ATTEMPTS = 12;
    private static final double[] STEERING_ANGLES = {
        0,
        Math.PI / 6,
        -Math.PI / 6,
        Math.PI / 3,
        -Math.PI / 3,
        Math.PI / 2,
        -Math.PI / 2,
        2 * Math.PI / 3,
        -2 * Math.PI / 3
    };

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
    private final List<DamageEvent> pendingDamageEvents;
    private double accumulatedDamage;
    private double damageEventTimer;
    private Vector2D lastDamagePosition;
    private double hungerDecayMultiplier;
    private double thirstDecayMultiplier;

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
        this.pendingDamageEvents = new ArrayList<>();
        this.accumulatedDamage = 0;
        this.damageEventTimer = 0;
        this.lastDamagePosition = position;
        this.hungerDecayMultiplier = 1.0;
        this.thirstDecayMultiplier = 1.0;
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
        updateDamageEventTimer(deltaTime);
    }

    /**
     * Thực hiện hành động đã quyết định bởi strategy.
     * Gọi bởi SimulationEngine sau khi strategy.decide().
     */

    public void executeAction(Action action, double deltaTime, WorldMap worldMap) {
        if (!alive) return;

        switch (action.getType()) {
            case IDLE -> doIdle(deltaTime);
            case WANDER -> doWander(deltaTime, worldMap);
            case MOVE_TO -> doMoveTo(action.getTargetPosition(), deltaTime, worldMap, false);
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
        hunger -= Constants.HUNGER_DECAY_RATE * hungerDecayMultiplier * deltaTime;
        if (hunger < 0) hunger = 0;

        // Khát dần
        thirst -= Constants.THIRST_DECAY_RATE * thirstDecayMultiplier * deltaTime;
        if (thirst < 0) thirst = 0;

        // Đói/khát → mất máu
        if (hunger <= 0) {
            takeDamage(10 * deltaTime);
        }
        if (thirst <= 0) {
            takeDamage(15 * deltaTime);
        }

        // Khi đói, tốc độ giảm
        if (hunger < Constants.CRITICAL_HUNGER) {
            speed = maxSpeed * 0.6;
        } else {
            speed = maxSpeed*0.85;
        }
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
            wanderTarget = findWanderTarget(worldMap);
            wanderTimer = 3 + random.nextDouble() * 5; // 3-8 giây
        }

        doMoveTo(wanderTarget, deltaTime, worldMap, false);
    }

    protected void doMoveTo(Vector2D target, double deltaTime, WorldMap worldMap, boolean isRunning) {
        if (target == null) return;

        Vector2D dir = position.directionTo(target);
        if (dir.magnitude() == 0) {
            doIdle(deltaTime);
            return;
        }
        TerrainType terrain = worldMap.getTerrainAt(position.getX(), position.getY());

        // Kiểm tra có thể đi trên terrain này không
        double terrainMod = getTerrainSpeedModifier(terrain);
        double moveSpeed = (isRunning ? speed * 1.3 : speed) * terrainMod;
        if (moveSpeed <= 0) {
            moveSpeed = maxSpeed * 0.5;
        }

        Vector2D newPos =
            findBestMovePosition(target, dir, moveSpeed * deltaTime, worldMap);

        // Kiểm tra vị trí mới có hợp lệ không
        if (newPos != null) {
            Vector2D moveDir = position.directionTo(newPos);
            position = newPos;
            direction = moveDir;
            setState(isRunning ? AnimalState.RUNNING : AnimalState.WALKING);
        } else {
            // Không đi được → đổi hướng
            wanderTarget = null;
            wanderTimer = 0;
        }
    }

    protected void doEat(Entity food, double deltaTime) {
        if (food == null || !food.isAlive()) return;

        if (canReach(food)) {
            setState(AnimalState.EATING);
            if (food instanceof Plant plant) {
                double nutrition = plant.beEaten();
                hunger = Math.min(Constants.MAX_HUNGER, hunger + nutrition);
                health = Math.min(maxHealth, health + 5);
            } else if (food instanceof Animal prey) {
                // Ăn thịt con mồi đã chết
                if (!prey.isAlive()) {
                    hunger = Math.min(Constants.MAX_HUNGER, hunger + 40);
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
        if (damage <= 0 || !alive) return;

        double oldHealth = health;
        health = Math.max(0, health - damage);
        double actualDamage = oldHealth - health;

        if (actualDamage > 0) {
            accumulatedDamage += actualDamage;
            lastDamagePosition = position;
        }

        if (health <= 0) {
            flushDamageEvent();
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

    public boolean canReach(Entity entity) {
        if (entity == null) return false;
        double reach = Math.max(MIN_INTERACTION_REACH, getSize() + entity.getSize() + INTERACTION_MARGIN);
        double dist = distanceTo(entity);
        return dist <= reach;
    }

    // Local steering helpers for terrain borders and obstacles.
    private Vector2D findWanderTarget(WorldMap worldMap) {
        for (int attempt = 0; attempt < WANDER_TARGET_ATTEMPTS; attempt++) {
            Vector2D candidate = position.add(Vector2D.random(10));
            if (isTraversablePosition(candidate, worldMap)) {
                return candidate;
            }
        }

        return position;
    }

    private Vector2D findBestMovePosition(
        Vector2D target,
        Vector2D preferredDir,
        double moveDistance,
        WorldMap worldMap
    ) {
        if (moveDistance <= 0) {
            return null;
        }

        double[] stepScales = {1.0, 0.5, 0.25};

        for (double stepScale : stepScales) {
            double step = moveDistance * stepScale;
            Vector2D best = null;
            double bestScore = Double.MAX_VALUE;

            for (double angle : STEERING_ANGLES) {
                Vector2D candidateDir = rotate(preferredDir, angle).normalize();
                if (candidateDir.magnitude() == 0) {
                    continue;
                }

                Vector2D candidate =
                    position.add(candidateDir.multiply(step));

                if (!isTraversablePosition(candidate, worldMap)) {
                    continue;
                }

                double score =
                    candidate.distanceTo(target)
                        + Math.abs(angle) * 0.05
                        + (1.0 - stepScale) * 0.2;

                if (score < bestScore) {
                    bestScore = score;
                    best = candidate;
                }
            }

            if (best != null) {
                return best;
            }
        }

        return null;
    }

    private boolean isTraversablePosition(Vector2D candidate, WorldMap worldMap) {
        if (!worldMap.isInBounds(candidate.getX(), candidate.getY())) {
            return false;
        }

        TerrainType terrain =
            worldMap.getTerrainAt(candidate.getX(), candidate.getY());

        return canTraverse(terrain);
    }

    private Vector2D rotate(Vector2D vector, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        return new Vector2D(
            vector.getX() * cos - vector.getY() * sin,
            vector.getX() * sin + vector.getY() * cos
        );
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

    public List<DamageEvent> consumeDamageEvents() {
        List<DamageEvent> events = new ArrayList<>(pendingDamageEvents);
        pendingDamageEvents.clear();
        return events;
    }

    private void updateDamageEventTimer(double deltaTime) {
        if (accumulatedDamage <= 0) return;

        damageEventTimer += deltaTime;
        if (damageEventTimer >= DAMAGE_EVENT_INTERVAL) {
            flushDamageEvent();
        }
    }

    private void flushDamageEvent() {
        if (accumulatedDamage <= 0) return;

        pendingDamageEvents.add(
            new DamageEvent(lastDamagePosition, accumulatedDamage)
        );
        accumulatedDamage = 0;
        damageEventTimer = 0;
    }

    public void setHunger(double hunger) { this.hunger = Math.max(0, Math.min(Constants.MAX_HUNGER, hunger)); }
    public void setThirst(double thirst) { this.thirst = Math.max(0, Math.min(Constants.MAX_THIRST, thirst)); }
    public void setNeedDecayMultipliers(double hungerMultiplier, double thirstMultiplier) {
        this.hungerDecayMultiplier = Math.max(0, hungerMultiplier);
        this.thirstDecayMultiplier = Math.max(0, thirstMultiplier);
    }
    
    public double getAge() { return age; }
    public double getReproductionCooldown() { return reproductionCooldown; }
}
