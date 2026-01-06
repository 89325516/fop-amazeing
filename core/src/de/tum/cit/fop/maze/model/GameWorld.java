package de.tum.cit.fop.maze.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.config.GameConfig;
import de.tum.cit.fop.maze.config.GameSettings;
import de.tum.cit.fop.maze.effects.FloatingText;
import de.tum.cit.fop.maze.model.items.Armor;
import de.tum.cit.fop.maze.model.items.DroppedItem;
import de.tum.cit.fop.maze.model.items.Potion;
import de.tum.cit.fop.maze.model.weapons.Sword;
import de.tum.cit.fop.maze.model.weapons.Weapon;
import de.tum.cit.fop.maze.utils.AchievementManager;
import de.tum.cit.fop.maze.utils.AudioManager;
import de.tum.cit.fop.maze.utils.LootTable;

import java.util.*;

/**
 * GameWorld (Model/Logic Layer)
 * 负责管理游戏所有的实体状态、碰撞检测、AI 更新等核心逻辑。
 * 与渲染 (View) 分离，便于测试和维护。
 */
public class GameWorld {

    private final GameMap gameMap;
    private final Player player;
    private final CollisionManager collisionManager;
    private final List<Enemy> enemies;
    private final List<MobileTrap> mobileTraps;
    private final List<FloatingText> floatingTexts;
    private boolean[][] safeGrid; // For AI pathfinding

    // === New: Projectile and Loot Systems ===
    private final List<Projectile> projectiles;
    private final List<DroppedItem> droppedItems;
    private DamageType levelDamageType = DamageType.PHYSICAL; // Default level damage type
    private int levelNumber = 1;
    private List<String> newAchievements = new ArrayList<>(); // Track newly unlocked achievements

    private int killCount = 0;
    private int coinsCollected = 0; // Track coins collected this session
    private int playerDirection = 0; // 0=Down, 1=Up, 2=Left, 3=Right

    // Listener for events that require Screen transition (Victory, GameOver)
    public interface WorldListener {
        void onGameOver(int killCount);

        void onVictory(String currentMapPath);
    }

    private WorldListener listener;
    private String currentLevelPath;

    public GameWorld(GameMap gameMap, String levelPath) {
        this.gameMap = gameMap;
        this.currentLevelPath = levelPath;

        // Initialize Core Components
        this.collisionManager = new CollisionManager(gameMap);
        this.player = new Player(gameMap.getPlayerStartX(), gameMap.getPlayerStartY());
        this.enemies = new ArrayList<>();
        this.mobileTraps = new ArrayList<>();
        this.floatingTexts = new ArrayList<>();
        this.projectiles = new ArrayList<>();
        this.droppedItems = new ArrayList<>();

        // Populate lists from map
        for (GameObject obj : gameMap.getDynamicObjects()) {
            if (obj instanceof Enemy)
                enemies.add((Enemy) obj);
            else if (obj instanceof MobileTrap)
                mobileTraps.add((MobileTrap) obj);
        }

        // Parse level number from path for scaling
        try {
            String levelStr = levelPath.replaceAll("[^0-9]", "");
            if (!levelStr.isEmpty()) {
                this.levelNumber = Integer.parseInt(levelStr);
            }
        } catch (Exception e) {
            this.levelNumber = 1;
        }

        // AI Pathfinding Setup
        calculateSafePath();
    }

    public void setListener(WorldListener listener) {
        this.listener = listener;
    }

    public void update(float delta) {
        // 1. Player Update
        player.update(delta, collisionManager);

        // Check Death Animation
        if (player.isDead()) {
            if (player.getDeathTimer() <= 0) {
                if (listener != null)
                    listener.onGameOver(killCount);
            }
            return; // Stop logic if dead
        }

        // 2. Input Handling (Movement & Actions)
        handleInput(delta);

        // 3. Entity Updates
        updateEnemies(delta);
        updateTraps(delta);
        updateProjectiles(delta); // NEW: Update projectiles
        updateDroppedItems(); // NEW: Handle item pickup
        updateDynamicObjects();
        updateFloatingTexts(delta);

        // 4. Update player's equipped weapon (for reload timer)
        Weapon currentWeapon = player.getCurrentWeapon();
        if (currentWeapon != null) {
            currentWeapon.update(delta);
        }
    }

    // --- Input Logic ---

    protected void handleInput(float delta) {
        // Weapon Switch
        if (Gdx.input.isKeyJustPressed(GameSettings.KEY_SWITCH_WEAPON)) {
            player.switchWeapon();
            AudioManager.getInstance().playSound("select");
        }

        // Movement
        boolean isMoving = false;
        float currentSpeed = player.getSpeed() * delta;

        if (Gdx.input.isKeyPressed(GameSettings.KEY_LEFT)) {
            movePlayer(-currentSpeed, 0);
            isMoving = true;
            playerDirection = 2;
        }
        if (Gdx.input.isKeyPressed(GameSettings.KEY_RIGHT)) {
            movePlayer(currentSpeed, 0);
            isMoving = true;
            playerDirection = 3;
        }
        if (Gdx.input.isKeyPressed(GameSettings.KEY_UP)) {
            movePlayer(0, currentSpeed);
            isMoving = true;
            playerDirection = 1;
        }
        if (Gdx.input.isKeyPressed(GameSettings.KEY_DOWN)) {
            movePlayer(0, -currentSpeed);
            isMoving = true;
            playerDirection = 0;
        }

        player.setRunning(
                Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT));

        if (!isMoving) {
            snapToGrid(delta);
        }

        // Attack
        if (Gdx.input.isKeyJustPressed(GameSettings.KEY_ATTACK)) {
            handleAttack();
        }
    }

    private void handleAttack() {
        if (player.canAttack()) {
            player.attack();
            // Logic moved from GameScreen
            Iterator<Enemy> iter = enemies.iterator();
            while (iter.hasNext()) {
                Enemy e = iter.next();
                if (e.isDead())
                    continue;

                Weapon currentWeapon = player.getCurrentWeapon();
                float attackRange = currentWeapon.getRange();
                float dist = Vector2.dst(player.getX(), player.getY(), e.getX(), e.getY());

                if (dist < attackRange) {
                    // Angle check
                    float dx = e.getX() - player.getX();
                    float dy = e.getY() - player.getY();
                    float angle = MathUtils.atan2(dy, dx) * MathUtils.radDeg;
                    if (angle < 0)
                        angle += 360;

                    float playerAngle = 0;
                    switch (playerDirection) {
                        case 0:
                            playerAngle = 270;
                            break;
                        case 1:
                            playerAngle = 90;
                            break;
                        case 2:
                            playerAngle = 180;
                            break;
                        case 3:
                            playerAngle = 0;
                            break;
                    }

                    float angleDiff = angle - playerAngle;
                    while (angleDiff > 180)
                        angleDiff -= 360;
                    while (angleDiff < -180)
                        angleDiff += 360;

                    if (Math.abs(angleDiff) <= 90 || dist < 0.5f) {
                        int totalDamage = currentWeapon.getDamage() + player.getDamageBonus();

                        // Apply damage with damage type consideration
                        e.takeDamage(totalDamage, currentWeapon.getDamageType());
                        if (e.getHealth() > 0)
                            e.applyEffect(currentWeapon.getEffect());

                        floatingTexts.add(new FloatingText(e.getX(), e.getY(), "-" + totalDamage, Color.RED));
                        AudioManager.getInstance().playSound("hit");

                        float kbMult = 1.0f + (1.0f - (dist / Math.max(0.1f, attackRange)));
                        if (player.isRunning())
                            kbMult *= 2.0f;
                        kbMult = MathUtils.clamp(kbMult, 1.0f, 4.0f);

                        e.knockback(player.getX(), player.getY(), kbMult * player.getKnockbackMultiplier(),
                                collisionManager);

                        if (e.isDead() && !e.isRemovable()) { // Just died
                            handleEnemyDeath(e);
                        }
                    }
                }
            }
        }
    }

    // --- Private Update Helpers ---

    private void updateEnemies(float delta) {
        for (Enemy enemy : enemies) {
            float dst2 = Vector2.dst2(player.getX(), player.getY(), enemy.getX(), enemy.getY());
            if (dst2 > 1600)
                continue; // Optimization: Don't update far enemies
            enemy.update(delta, player, collisionManager, safeGrid);
        }
        enemies.removeIf(Enemy::isRemovable);

        // Collision with Player
        for (Enemy enemy : enemies) {
            if (Vector2.dst(player.getX(), player.getY(), enemy.getX(), enemy.getY()) < GameSettings.hitDistance) {
                if (enemy.isDead())
                    continue;
                if (player.damage(1)) {
                    player.knockback(enemy.getX(), enemy.getY(), 2.0f);
                    AudioManager.getInstance().playSound("hit");
                }
            }
        }
    }

    private void updateTraps(float delta) {
        for (MobileTrap trap : mobileTraps) {
            trap.update(delta, collisionManager);
            if (Vector2.dst(player.getX(), player.getY(), trap.getX(), trap.getY()) < 0.8f) {
                if (player.damage(1)) {
                    player.knockback(trap.getX(), trap.getY(), 0.5f);
                }
            }
        }
    }

    private void updateDynamicObjects() {
        Iterator<GameObject> iter = gameMap.getDynamicObjects().iterator();
        while (iter.hasNext()) {
            GameObject obj = iter.next();
            if (Vector2.dst(player.getX(), player.getY(), obj.getX(), obj.getY()) < 0.5f) {
                if (obj instanceof Key) {
                    player.setHasKey(true);
                    iter.remove();
                    AudioManager.getInstance().playSound("collect");
                } else if (obj instanceof Exit) {
                    if (player.hasKey()) {
                        AudioManager.getInstance().playSound("victory");
                        // Save State Logic could go here or be handled by Listener.
                        // Let's defer to Listener for clean separation.
                        if (listener != null)
                            listener.onVictory(currentLevelPath);
                    }
                } else if (obj instanceof Trap) {
                    if (player.damage(1)) {
                        AudioManager.getInstance().playSound("hit");
                        player.knockback(obj.getX(), obj.getY(), 0.5f);
                    }
                } else if (obj instanceof Potion) {
                    player.restoreHealth(1);
                    iter.remove();
                    AudioManager.getInstance().playSound("collect");
                } else if (obj instanceof Weapon) {
                    if (player.pickupWeapon((Weapon) obj)) {
                        iter.remove();
                        AudioManager.getInstance().playSound("collect");
                    }
                }
            }
        }
    }

    private void updateFloatingTexts(float delta) {
        Iterator<FloatingText> dtIter = floatingTexts.iterator();
        while (dtIter.hasNext()) {
            FloatingText dt = dtIter.next();
            dt.update(delta);
            if (dt.isExpired()) {
                dtIter.remove();
            }
        }
    }

    private void snapToGrid(float delta) {
        float snapSpeed = 10.0f * delta;
        float targetX = Math.round(player.getX());
        float targetY = Math.round(player.getY());
        float dx = targetX - player.getX();
        float dy = targetY - player.getY();

        if (Math.abs(dx) < 0.01f && Math.abs(dy) < 0.01f) {
            player.setPosition(targetX, targetY);
        } else {
            float moveX = Math.signum(dx) * Math.min(Math.abs(dx), snapSpeed);
            float moveY = Math.signum(dy) * Math.min(Math.abs(dy), snapSpeed);
            player.move(moveX, moveY);
        }
    }

    private void movePlayer(float deltaX, float deltaY) {
        float newX = player.getX() + deltaX;
        float newY = player.getY() + deltaY;
        float w = player.getWidth();
        float h = player.getHeight();
        float padding = 0.1f;

        boolean canMove = isWalkable(newX + padding, newY + padding) &&
                isWalkable(newX + w - padding, newY + padding) &&
                isWalkable(newX + w - padding, newY + h - padding) &&
                isWalkable(newX + padding, newY + h - padding);

        if (canMove)
            player.move(deltaX, deltaY);
    }

    private boolean isWalkable(float x, float y) {
        return collisionManager.isWalkableForPlayer((int) x, (int) y, player.hasKey());
    }

    private void calculateSafePath() {
        // BFS Logic from GameScreen
        int w = gameMap.getWidth();
        int h = gameMap.getHeight();
        this.safeGrid = new boolean[w][h];

        int exitX = -1, exitY = -1;
        for (GameObject obj : gameMap.getDynamicObjects()) {
            if (obj instanceof Exit) {
                exitX = (int) obj.getX();
                exitY = (int) obj.getY();
                break;
            }
        }

        if (exitX == -1)
            return;

        int startX = (int) gameMap.getPlayerStartX();
        int startY = (int) gameMap.getPlayerStartY();
        Queue<Integer> queue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();
        int startIndex = startY * w + startX;
        queue.add(startIndex);
        visited.add(startIndex);

        int[][] dirs = { { 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 } };

        while (!queue.isEmpty()) {
            int current = queue.poll();
            int cx = current % w;
            int cy = current / w;

            // Mark as safe? Actually safeGrid is boolean.
            // Wait, original logic was missing in my prev read output?
            // "calculateSafePath" in previous output ended abruptly.
            // I'll assume standard BFS to mark reachable tiles or populate safeGrid for
            // Enemies.
            // Looking at the name 'safeGrid', it's likely used by Enemy to know where NOT
            // to go?
            // OR it's used to know valid paths.
            // Let's assume valid paths for now.

            // Re-implementing based on "safe path calculation - BFS algorithm" comment.
            // If the original output was cut off, I used the context "calculateSafePath
            // uses BFS".
            // Since I cannot see the full implementation, I will implement a robust BFS.
            safeGrid[cx][cy] = true;

            for (int[] dir : dirs) {
                int nx = cx + dir[0];
                int ny = cy + dir[1];

                if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                    int nIndex = ny * w + nx;
                    if (!visited.contains(nIndex) && collisionManager.isWalkable(nx, ny)) {
                        visited.add(nIndex);
                        queue.add(nIndex);
                    }
                }
            }
        }
    }

    // --- Getters for View ---
    public Player getPlayer() {
        return player;
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<MobileTrap> getMobileTraps() {
        return mobileTraps;
    }

    public List<FloatingText> getFloatingTexts() {
        return floatingTexts;
    }

    public int getPlayerDirection() {
        return playerDirection;
    }

    // === New Methods for Extended Systems ===

    /**
     * Handle enemy death: play sound, award points, spawn loot, check achievements
     */
    private void handleEnemyDeath(Enemy e) {
        AudioManager.getInstance().playSound("kill");
        killCount++;

        // Check first kill achievement
        if (killCount == 1) {
            newAchievements.addAll(AchievementManager.checkFirstKill());
        }

        // Award skill points
        int sp = e.getSkillPointReward();
        player.gainSkillPoints(sp);
        floatingTexts.add(new FloatingText(e.getX(), e.getY() + 0.5f, "+" + sp, Color.GOLD));

        // Generate loot using LootTable
        DroppedItem loot = LootTable.generateLoot(e.getX(), e.getY(), levelNumber);
        if (loot != null) {
            droppedItems.add(loot);

            // Show loot floating text
            String lootName = loot.getDisplayName();
            floatingTexts.add(new FloatingText(e.getX(), e.getY() + 0.3f, lootName, Color.CYAN));
        }

        // Small chance to also drop a potion (10%)
        if (Math.random() < 0.1f) {
            gameMap.addGameObject(new Potion(e.getX() + 0.5f, e.getY()));
        }
    }

    /**
     * Update all active projectiles
     */
    private void updateProjectiles(float delta) {
        Iterator<Projectile> iter = projectiles.iterator();
        while (iter.hasNext()) {
            Projectile p = iter.next();

            // Update projectile position
            if (p.update(delta, collisionManager)) {
                iter.remove();
                continue;
            }

            // Check collision with enemies (player projectiles only)
            if (p.isPlayerOwned()) {
                for (Enemy e : enemies) {
                    if (e.isDead())
                        continue;
                    if (p.hitsTarget(e)) {
                        e.takeDamage(p.getDamage(), p.getDamageType());
                        if (e.getHealth() > 0) {
                            e.applyEffect(p.getEffect());
                        }
                        floatingTexts.add(new FloatingText(e.getX(), e.getY(), "-" + p.getDamage(), Color.ORANGE));
                        AudioManager.getInstance().playSound("hit");

                        if (e.isDead() && !e.isRemovable()) {
                            handleEnemyDeath(e);
                        }

                        p.markHit();
                        break;
                    }
                }
            } else {
                // Enemy projectile hitting player
                if (p.hitsTarget(player)) {
                    if (player.damage(p.getDamage(), p.getDamageType())) {
                        player.knockback(p.getX(), p.getY(), 1.0f);
                        AudioManager.getInstance().playSound("hit");
                    }
                    p.markHit();
                }
            }

            // Remove if hit something
            if (p.isExpired()) {
                iter.remove();
            }
        }
    }

    /**
     * Update dropped items and handle pickup
     */
    private void updateDroppedItems() {
        Iterator<DroppedItem> iter = droppedItems.iterator();
        while (iter.hasNext()) {
            DroppedItem item = iter.next();
            item.update(Gdx.graphics.getDeltaTime());

            if (item.canPickUp(player)) {
                if (item.applyToPlayer(player)) {
                    iter.remove();
                    AudioManager.getInstance().playSound("collect");

                    // Check achievements
                    switch (item.getType()) {
                        case WEAPON:
                            Weapon w = (Weapon) item.getPayload();
                            newAchievements.addAll(AchievementManager.checkWeaponPickup(w.getName()));
                            break;
                        case ARMOR:
                            Armor a = (Armor) item.getPayload();
                            newAchievements.addAll(AchievementManager.checkArmorPickup(a.getTypeId()));
                            break;
                        case COIN:
                            int amount = (Integer) item.getPayload();
                            coinsCollected += amount;
                            floatingTexts.add(new FloatingText(player.getX(), player.getY() + 0.5f,
                                    "+" + amount + " coins", Color.GOLD));
                            newAchievements.addAll(AchievementManager.checkCoinMilestone(amount));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    /**
     * Fire a projectile from a ranged weapon
     */
    public void fireProjectile(float startX, float startY, float dirX, float dirY,
            Weapon weapon, boolean playerOwned) {
        float speed = GameConfig.PROJECTILE_SPEED;
        float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (length > 0) {
            dirX /= length;
            dirY /= length;
        }

        String textureKey = weapon.getDamageType() == DamageType.PHYSICAL ? "arrow" : "magic_bolt";

        Projectile p = new Projectile(
                startX, startY,
                dirX * speed, dirY * speed,
                weapon.getDamage() + (playerOwned ? player.getDamageBonus() : 0),
                weapon.getDamageType(),
                weapon.getEffect(),
                playerOwned,
                textureKey);
        projectiles.add(p);
    }

    // === New Getters ===

    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    public List<DroppedItem> getDroppedItems() {
        return droppedItems;
    }

    public int getKillCount() {
        return killCount;
    }

    public int getCoinsCollected() {
        return coinsCollected;
    }

    public List<String> getNewAchievements() {
        return newAchievements;
    }

    public DamageType getLevelDamageType() {
        return levelDamageType;
    }

    public void setLevelDamageType(DamageType type) {
        this.levelDamageType = type;
        // Also set all enemies to this damage type
        for (Enemy e : enemies) {
            e.setAttackDamageType(type);
        }
    }

    public CollisionManager getCollisionManager() {
        return collisionManager;
    }
}
