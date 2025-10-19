import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.swing.*;

/**
 * Lesson 7: Health and Damage System Implementation
 * 
 * This builds on the InventoryGame and adds:
 * 1. Player health and damage system
 * 2. Environmental hazards (lava damage)
 * 3. Simple enemies that deal damage
 * 4. Health regeneration over time
 * 5. Visual health bar and damage indicators
 * 6. Death and respawn mechanics
 */

enum TileType {
    AIR(0, Color.BLACK, false, false),
    DIRT(1, new Color(139, 69, 19), true, false),
    STONE(2, Color.GRAY, true, false),
    GRASS(3, Color.GREEN, true, false),
    SAND(4, new Color(194, 178, 128), true, false),
    LAVA(5, new Color(255, 100, 0), false, true), // New: Damaging tile
    COAL_ORE(6, new Color(64, 64, 64), true, false),
    IRON_ORE(7, new Color(205, 127, 50), true, false),
    GOLD_ORE(8, new Color(255, 215, 0), true, false);
    
    private final int id;
    private final Color color;
    private final boolean solid;
    private final boolean damaging;
    
    TileType(int id, Color color, boolean solid, boolean damaging) {
        this.id = id;
        this.color = color;
        this.solid = solid;
        this.damaging = damaging;
    }
    
    public int getId() { return id; }
    public Color getColor() { return color; }
    public boolean isSolid() { return solid; }
    public boolean isDamaging() { return damaging; }
}

class ItemStack {
    private TileType itemType;
    private int quantity;
    private int maxStackSize;
    
    public ItemStack(TileType type, int quantity) {
        this.itemType = type;
        this.quantity = quantity;
        this.maxStackSize = getMaxStackSize(type);
    }
    
    private int getMaxStackSize(TileType type) {
        switch (type) {
            case DIRT:
            case STONE:
            case SAND:
                return 64;
            case COAL_ORE:
            case IRON_ORE:
            case GOLD_ORE:
                return 32;
            default:
                return 1;
        }
    }
    
    public boolean canAddMore() {
        return quantity < maxStackSize;
    }
    
    public void addQuantity(int amount) {
        quantity = Math.min(maxStackSize, quantity + amount);
    }
    
    public void removeQuantity(int amount) {
        quantity = Math.max(0, quantity - amount);
    }
    
    public TileType getItemType() { return itemType; }
    public int getQuantity() { return quantity; }
    public boolean isEmpty() { return quantity <= 0; }
    public int getMaxStackSize() { return maxStackSize; }
}

class DamageIndicator {
    private int x, y;
    private int damage;
    private long creationTime;
    private static final long DURATION = 2000; // 2 seconds
    
    public DamageIndicator(int x, int y, int damage) {
        this.x = x;
        this.y = y;
        this.damage = damage;
        this.creationTime = System.currentTimeMillis();
    }
    
    public void update() {
        y -= 1; // Float upward
    }
    
    public void draw(Graphics2D g2d, Camera camera) {
        long age = System.currentTimeMillis() - creationTime;
        if (age > DURATION) return;
        
        float alpha = 1.0f - (float) age / DURATION;
        g2d.setColor(new Color(255, 0, 0, (int) (255 * alpha)));
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("-" + damage, x - camera.getX(), y - camera.getY());
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() - creationTime > DURATION;
    }
}

class Enemy {
    private float x, y;
    private float velocityX, velocityY;
    private int width, height;
    private int health;
    private boolean onGround;
    private World world;
    private Player target;
    private AIState state;
    private long lastAttackTime = 0;
    private static final long ATTACK_COOLDOWN = 2000; // 2 seconds
    
    enum AIState { WANDER, CHASE, ATTACK }
    
    private static final float GRAVITY = 0.3f;
    private static final float MOVE_SPEED = 1.5f;
    private static final int DAMAGE = 20;
    private static final float CHASE_RANGE = 80.0f;
    private static final float ATTACK_RANGE = 25.0f;
    
    public Enemy(float startX, float startY, World world, Player target) {
        this.x = startX;
        this.y = startY;
        this.world = world;
        this.target = target;
        this.width = 16;
        this.height = 20;
        this.health = 50;
        this.velocityX = 0;
        this.velocityY = 0;
        this.onGround = false;
        this.state = AIState.WANDER;
    }
    
    public void update() {
        updateAI();
        applyPhysics();
        
        // Check if touching player
        if (isCollidingWithPlayer() && canAttack()) {
            target.takeDamage(DAMAGE);
            lastAttackTime = System.currentTimeMillis();
        }
    }
    
    private void updateAI() {
        float distToPlayer = distance(x, y, target.getX(), target.getY());
        
        switch (state) {
            case WANDER:
                if (distToPlayer < CHASE_RANGE) {
                    state = AIState.CHASE;
                } else {
                    // Random wandering
                    if (Math.random() < 0.01) {
                        velocityX = (Math.random() < 0.5) ? -MOVE_SPEED : MOVE_SPEED;
                    }
                    if (Math.random() < 0.005) {
                        velocityX = 0; // Stop moving sometimes
                    }
                }
                break;
                
            case CHASE:
                if (distToPlayer > CHASE_RANGE * 1.5) {
                    state = AIState.WANDER;
                    velocityX = 0;
                } else if (distToPlayer < ATTACK_RANGE) {
                    state = AIState.ATTACK;
                    velocityX = 0;
                } else {
                    // Move toward player
                    if (target.getX() < x) {
                        velocityX = -MOVE_SPEED;
                    } else {
                        velocityX = MOVE_SPEED;
                    }
                    
                    // Jump if there's a wall
                    if (checkWallInDirection(velocityX > 0 ? 1 : -1) && onGround) {
                        velocityY = -8.0f;
                    }
                }
                break;
                
            case ATTACK:
                if (distToPlayer > ATTACK_RANGE) {
                    state = AIState.CHASE;
                } else {
                    // Stay close and attack
                    velocityX = 0;
                }
                break;
        }
    }
    
    private boolean checkWallInDirection(int dir) {
        int checkX = (int) ((x + width * dir) / World.TILE_SIZE);
        int checkY = (int) (y / World.TILE_SIZE);
        return world.isSolid(checkX, checkY);
    }
    
    private void applyPhysics() {
        if (!onGround) {
            velocityY += GRAVITY;
        }
        
        moveHorizontal(velocityX);
        moveVertical(velocityY);
    }
    
    private void moveHorizontal(float deltaX) {
        x += deltaX;
        if (checkCollision()) {
            x -= deltaX;
        }
    }
    
    private void moveVertical(float deltaY) {
        y += deltaY;
        if (checkCollision()) {
            y -= deltaY;
            if (deltaY > 0) {
                onGround = true;
            }
            velocityY = 0;
        } else {
            onGround = false;
        }
    }
    
    private boolean checkCollision() {
        int left = (int) (x / World.TILE_SIZE);
        int right = (int) ((x + width) / World.TILE_SIZE);
        int top = (int) (y / World.TILE_SIZE);
        int bottom = (int) ((y + height) / World.TILE_SIZE);
        
        return world.isSolid(left, top) || world.isSolid(right, top) ||
               world.isSolid(left, bottom) || world.isSolid(right, bottom);
    }
    
    private boolean isCollidingWithPlayer() {
        return x < target.getX() + 12 && x + width > target.getX() &&
               y < target.getY() + 24 && y + height > target.getY();
    }
    
    private boolean canAttack() {
        return System.currentTimeMillis() - lastAttackTime > ATTACK_COOLDOWN;
    }
    
    private float distance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }
    
    public void draw(Graphics2D g2d, Camera camera) {
        int drawX = (int) (x - camera.getX());
        int drawY = (int) (y - camera.getY());
        
        // Enemy body
        g2d.setColor(Color.RED);
        g2d.fillRect(drawX, drawY, width, height);
        
        // Enemy outline
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRect(drawX, drawY, width, height);
        
        // Simple eyes
        g2d.setColor(Color.WHITE);
        g2d.fillOval(drawX + 3, drawY + 3, 3, 3);
        g2d.fillOval(drawX + 10, drawY + 3, 3, 3);
        
        // Health bar above enemy
        int barWidth = 20;
        int barHeight = 4;
        float healthPercent = (float) health / 50;
        
        g2d.setColor(Color.BLACK);
        g2d.fillRect(drawX - 2, drawY - 8, barWidth, barHeight);
        g2d.setColor(Color.RED);
        g2d.fillRect(drawX - 2, drawY - 8, (int) (barWidth * healthPercent), barHeight);
    }
    
    public void takeDamage(int damage) {
        health -= damage;
    }
    
    public boolean isDead() {
        return health <= 0;
    }
    
    public int getX() { return (int) x; }
    public int getY() { return (int) y; }
}

class SimplexNoise {
    private final Random random;
    
    public SimplexNoise(long seed) {
        this.random = new Random(seed);
    }
    
    public double noise(double x, double y) {
        int intX = (int) Math.floor(x);
        int intY = (int) Math.floor(y);
        
        double fracX = x - intX;
        double fracY = y - intY;
        
        double n1 = dotGridGradient(intX, intY, x, y);
        double n2 = dotGridGradient(intX + 1, intY, x, y);
        double ix1 = interpolate(n1, n2, fracX);
        
        n1 = dotGridGradient(intX, intY + 1, x, y);
        n2 = dotGridGradient(intX + 1, intY + 1, x, y);
        double ix2 = interpolate(n1, n2, fracX);
        
        return interpolate(ix1, ix2, fracY);
    }
    
    private double dotGridGradient(int ix, int iy, double x, double y) {
        random.setSeed(ix * 73856093L + iy * 19349663L);
        double dx = x - ix;
        double dy = y - iy;
        
        double gradientX = random.nextGaussian();
        double gradientY = random.nextGaussian();
        
        return dx * gradientX + dy * gradientY;
    }
    
    private double interpolate(double a, double b, double t) {
        t = t * t * (3.0 - 2.0 * t);
        return a + t * (b - a);
    }
}

class World {
    private static final int WORLD_WIDTH = 150;
    private static final int WORLD_HEIGHT = 80;
    public static final int TILE_SIZE = 16;
    
    private TileType[][] tiles;
    private SimplexNoise heightNoise;
    private SimplexNoise caveNoise;
    private Random random;
    
    public World(long seed) {
        tiles = new TileType[WORLD_WIDTH][WORLD_HEIGHT];
        heightNoise = new SimplexNoise(seed);
        caveNoise = new SimplexNoise(seed + 1000);
        random = new Random(seed);
        generateWorld();
    }
    
    private void generateWorld() {
        generateTerrain();
        generateCaves();
        placeOres();
        placeLava(); // New: Add lava pools
    }
    
    private void generateTerrain() {
        for (int x = 0; x < WORLD_WIDTH; x++) {
            double heightValue = heightNoise.noise(x * 0.01, 0) * 0.5 + 0.5;
            int surfaceHeight = (int) (20 + heightValue * 15);
            
            for (int y = 0; y < WORLD_HEIGHT; y++) {
                if (y < surfaceHeight) {
                    tiles[x][y] = TileType.AIR;
                } else if (y < surfaceHeight + 1) {
                    tiles[x][y] = TileType.GRASS;
                } else if (y < surfaceHeight + 4) {
                    tiles[x][y] = TileType.DIRT;
                } else {
                    tiles[x][y] = TileType.STONE;
                }
            }
        }
    }
    
    private void generateCaves() {
        for (int x = 0; x < WORLD_WIDTH; x++) {
            for (int y = 25; y < WORLD_HEIGHT - 5; y++) {
                double caveValue = caveNoise.noise(x * 0.05, y * 0.05);
                if (caveValue > 0.2) {
                    tiles[x][y] = TileType.AIR;
                }
            }
        }
    }
    
    private void placeOres() {
        for (int x = 0; x < WORLD_WIDTH; x++) {
            for (int y = 30; y < WORLD_HEIGHT; y++) {
                if (tiles[x][y] == TileType.STONE) {
                    random.setSeed(x * 12345L + y * 67890L);
                    double oreChance = random.nextDouble();
                    
                    if (oreChance < 0.008 && y > 55) {
                        tiles[x][y] = TileType.GOLD_ORE;
                    } else if (oreChance < 0.025 && y > 40) {
                        tiles[x][y] = TileType.IRON_ORE;
                    } else if (oreChance < 0.06 && y > 30) {
                        tiles[x][y] = TileType.COAL_ORE;
                    }
                }
            }
        }
    }
    
    private void placeLava() {
        // Add lava pools in deep areas
        for (int x = 0; x < WORLD_WIDTH; x++) {
            for (int y = WORLD_HEIGHT - 10; y < WORLD_HEIGHT; y++) {
                if (tiles[x][y] == TileType.AIR) {
                    random.setSeed(x * 54321L + y * 98765L);
                    if (random.nextDouble() < 0.3) {
                        tiles[x][y] = TileType.LAVA;
                    }
                }
            }
        }
    }
    
    public TileType getTile(int x, int y) {
        if (x < 0 || x >= WORLD_WIDTH || y < 0 || y >= WORLD_HEIGHT) {
            return TileType.STONE;
        }
        return tiles[x][y];
    }
    
    public void setTile(int x, int y, TileType type) {
        if (x >= 0 && x < WORLD_WIDTH && y >= 0 && y < WORLD_HEIGHT) {
            tiles[x][y] = type;
        }
    }
    
    public boolean isSolid(int x, int y) {
        return getTile(x, y).isSolid();
    }
    
    public boolean isDamaging(int x, int y) {
        return getTile(x, y).isDamaging();
    }
    
    public int getWorldWidth() { return WORLD_WIDTH; }
    public int getWorldHeight() { return WORLD_HEIGHT; }
}

class Camera {
    private int x;
    private int y;
    private int screenWidth;
    private int screenHeight;
    
    public Camera(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }
    
    public void update(int targetX, int targetY) {
        x = targetX - screenWidth / 2;
        y = targetY - screenHeight / 2;
        
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x > World.TILE_SIZE * 150 - screenWidth) {
            x = World.TILE_SIZE * 150 - screenWidth;
        }
        if (y > World.TILE_SIZE * 80 - screenHeight) {
            y = World.TILE_SIZE * 80 - screenHeight;
        }
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
}

class Player {
    private float x;
    private float y;
    private float velocityX;
    private float velocityY;
    private int width;
    private int height;
    private boolean onGround;
    private World world;
    
    // Inventory system
    private ItemStack[] inventory = new ItemStack[36];
    private ItemStack[] hotbar = new ItemStack[9];
    private int selectedSlot = 0;
    
    // Health system
    private int health = 100;
    private int maxHealth = 100;
    private long lastDamageTime = 0;
    private long lastRegenTime = 0;
    private static final long DAMAGE_COOLDOWN = 1000; // 1 second invincibility
    private static final long HEALTH_REGEN_INTERVAL = 5000; // 5 seconds
    private static final int REGEN_AMOUNT = 5;
    
    // Visual effects
    private boolean isInvulnerable = false;
    private int invulnerabilityFlashTimer = 0;
    
    private static final float GRAVITY = 0.5f;
    private static final float JUMP_STRENGTH = -12.0f;
    private static final float MOVE_SPEED = 3.0f;
    
    public Player(float startX, float startY, World world) {
        this.x = startX;
        this.y = startY;
        this.world = world;
        this.width = 12;
        this.height = 24;
        this.velocityX = 0;
        this.velocityY = 0;
        this.onGround = false;
        
        // Initialize hotbar
        for (int i = 0; i < 9; i++) {
            hotbar[i] = inventory[i];
        }
        
        // Give starting items
        collectItem(TileType.DIRT, 20);
        collectItem(TileType.STONE, 15);
    }
    
    public void update() {
        updatePhysics();
        updateHealth();
        updateVisualEffects();
        checkEnvironmentalDamage();
    }
    
    private void updatePhysics() {
        if (!onGround) {
            velocityY += GRAVITY;
        }
        
        moveHorizontal(velocityX);
        moveVertical(velocityY);
        
        velocityX = 0;
    }
    
    private void updateHealth() {
        long currentTime = System.currentTimeMillis();
        
        // Health regeneration
        if (health < maxHealth && currentTime - lastRegenTime > HEALTH_REGEN_INTERVAL) {
            heal(REGEN_AMOUNT);
            lastRegenTime = currentTime;
        }
        
        // Update invulnerability
        if (isInvulnerable && currentTime - lastDamageTime > DAMAGE_COOLDOWN) {
            isInvulnerable = false;
        }
    }
    
    private void updateVisualEffects() {
        if (isInvulnerable) {
            invulnerabilityFlashTimer++;
        } else {
            invulnerabilityFlashTimer = 0;
        }
    }
    
    private void checkEnvironmentalDamage() {
        // Check if standing in damaging tiles (like lava)
        int tileX = (int) ((x + width / 2) / World.TILE_SIZE);
        int tileY = (int) ((y + height / 2) / World.TILE_SIZE);
        
        if (world.isDamaging(tileX, tileY)) {
            takeDamage(10); // Lava damage
        }
    }
    
    public boolean takeDamage(int damage) {
        long currentTime = System.currentTimeMillis();
        
        if (!isInvulnerable) {
            health = Math.max(0, health - damage);
            lastDamageTime = currentTime;
            isInvulnerable = true;
            
            if (health <= 0) {
                die();
            }
            
            return true; // Damage was dealt
        }
        return false; // No damage (invulnerable)
    }
    
    public void heal(int healAmount) {
        health = Math.min(maxHealth, health + healAmount);
        lastRegenTime = System.currentTimeMillis(); // Reset regen timer
    }
    
    private void die() {
        // Respawn logic
        health = maxHealth;
        x = World.TILE_SIZE * 75; // Spawn position
        y = World.TILE_SIZE * 10;
        
        // Could add: drop items, death message, etc.
        System.out.println("Player died and respawned!");
    }
    
    public void collectItem(TileType itemType, int quantity) {
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null && 
                inventory[i].getItemType() == itemType && 
                inventory[i].canAddMore()) {
                
                int canAdd = Math.min(quantity, 
                    inventory[i].getMaxStackSize() - inventory[i].getQuantity());
                inventory[i].addQuantity(canAdd);
                quantity -= canAdd;
                
                if (quantity <= 0) {
                    updateHotbar();
                    return;
                }
            }
        }
        
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] == null && quantity > 0) {
                ItemStack newStack = new ItemStack(itemType, 1);
                int stackAmount = Math.min(quantity, newStack.getMaxStackSize());
                inventory[i] = new ItemStack(itemType, stackAmount);
                quantity -= stackAmount;
            }
        }
        
        updateHotbar();
    }
    
    private void updateHotbar() {
        for (int i = 0; i < 9; i++) {
            hotbar[i] = inventory[i];
        }
    }
    
    public ItemStack getSelectedItem() {
        return hotbar[selectedSlot];
    }
    
    public void selectSlot(int slot) {
        if (slot >= 0 && slot < hotbar.length) {
            selectedSlot = slot;
        }
    }
    
    public boolean useSelectedItem() {
        ItemStack selected = getSelectedItem();
        if (selected != null && !selected.isEmpty()) {
            selected.removeQuantity(1);
            if (selected.isEmpty()) {
                inventory[selectedSlot] = null;
                hotbar[selectedSlot] = null;
            }
            return true;
        }
        return false;
    }
    
    private void moveHorizontal(float deltaX) {
        x += deltaX;
        if (checkCollision()) {
            x -= deltaX;
        }
    }
    
    private void moveVertical(float deltaY) {
        y += deltaY;
        if (checkCollision()) {
            y -= deltaY;
            if (deltaY > 0) {
                onGround = true;
            }
            velocityY = 0;
        } else {
            onGround = false;
        }
    }
    
    private boolean checkCollision() {
        int left = (int) (x / World.TILE_SIZE);
        int right = (int) ((x + width) / World.TILE_SIZE);
        int top = (int) (y / World.TILE_SIZE);
        int bottom = (int) ((y + height) / World.TILE_SIZE);
        
        return world.isSolid(left, top) || world.isSolid(right, top) ||
               world.isSolid(left, bottom) || world.isSolid(right, bottom);
    }
    
    public void moveLeft() { velocityX = -MOVE_SPEED; }
    public void moveRight() { velocityX = MOVE_SPEED; }
    
    public void jump() {
        if (onGround) {
            velocityY = JUMP_STRENGTH;
            onGround = false;
        }
    }
    
    public void draw(Graphics2D g2d, Camera camera) {
        int drawX = (int) (x - camera.getX());
        int drawY = (int) (y - camera.getY());
        
        // Flash effect when invulnerable
        if (isInvulnerable && invulnerabilityFlashTimer % 10 < 5) {
            return; // Skip drawing (flash effect)
        }
        
        g2d.setColor(Color.BLUE);
        g2d.fillRect(drawX, drawY, width, height);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(drawX, drawY, width, height);
    }
    
    // Getters
    public int getX() { return (int) x; }
    public int getY() { return (int) y; }
    public int getCenterX() { return (int) (x + (double) width / 2); }
    public int getCenterY() { return (int) (y + (double) height / 2); }
    public int getSelectedSlot() { return selectedSlot; }
    public ItemStack[] getHotbar() { return hotbar; }
    public ItemStack[] getInventory() { return inventory; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public boolean isInvulnerable() { return isInvulnerable; }
}

public class HealthDamageGame extends JPanel implements Runnable, KeyListener, MouseListener {
    
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final int TARGET_FPS = 60;
    private static final long TARGET_TIME = 1000000000 / TARGET_FPS;
    
    private boolean running = false;
    private Thread gameThread;
    private World world;
    private Player player;
    private Camera camera;
    private List<Enemy> enemies;
    private List<DamageIndicator> damageIndicators;
    
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean spacePressed = false;
    
    public HealthDamageGame() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        
        long seed = System.currentTimeMillis();
        world = new World(seed);
        player = new Player(World.TILE_SIZE * 75, World.TILE_SIZE * 10, world);
        camera = new Camera(WINDOW_WIDTH, WINDOW_HEIGHT);
        enemies = new ArrayList<>();
        damageIndicators = new ArrayList<>();
        
        // Spawn some enemies
        spawnEnemies();
        
        System.out.println("Health and Damage system initialized!");
        System.out.println("Controls: A/D - Move, Space - Jump, 1-9 - Select slot");
        System.out.println("Red enemies will chase and attack you!");
        System.out.println("Avoid lava (orange blocks) - they deal damage!");
    }
    
    private void spawnEnemies() {
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            float enemyX = random.nextInt(world.getWorldWidth() * World.TILE_SIZE);
            float enemyY = World.TILE_SIZE * 15; // Spawn near surface
            enemies.add(new Enemy(enemyX, enemyY, world, player));
        }
    }
    
    public void startGame() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
    
    public void stopGame() {
        running = false;
        try {
            if (gameThread != null) {
                gameThread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public void run() {
        while (running) {
            long startTime = System.nanoTime();
            
            update();
            repaint();
            
            long elapsed = System.nanoTime() - startTime;
            long waitTime = TARGET_TIME - elapsed;
            
            if (waitTime > 0) {
                try {
                    Thread.sleep(waitTime / 1000000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
    
    private void update() {
        if (leftPressed) player.moveLeft();
        if (rightPressed) player.moveRight();
        if (spacePressed) player.jump();
        
        player.update();
        camera.update(player.getCenterX(), player.getCenterY());
        
        // Update enemies
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            enemy.update();
            
            if (enemy.isDead()) {
                enemyIterator.remove();
            }
        }
        
        // Update damage indicators
        Iterator<DamageIndicator> indicatorIterator = damageIndicators.iterator();
        while (indicatorIterator.hasNext()) {
            DamageIndicator indicator = indicatorIterator.next();
            indicator.update();
            
            if (indicator.isExpired()) {
                indicatorIterator.remove();
            }
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        drawWorld(g2d);
        
        // Draw enemies
        for (Enemy enemy : enemies) {
            enemy.draw(g2d, camera);
        }
        
        player.draw(g2d, camera);
        
        // Draw damage indicators
        for (DamageIndicator indicator : damageIndicators) {
            indicator.draw(g2d, camera);
        }
        
        drawUI(g2d);
        drawHotbar(g2d);
        drawHealthBar(g2d);
    }
    
    private void drawWorld(Graphics2D g2d) {
        int startX = Math.max(0, camera.getX() / World.TILE_SIZE);
        int endX = Math.min(world.getWorldWidth(), 
                           (camera.getX() + WINDOW_WIDTH) / World.TILE_SIZE + 1);
        int startY = Math.max(0, camera.getY() / World.TILE_SIZE);
        int endY = Math.min(world.getWorldHeight(), 
                           (camera.getY() + WINDOW_HEIGHT) / World.TILE_SIZE + 1);
        
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                TileType tile = world.getTile(x, y);
                if (tile != TileType.AIR) {
                    g2d.setColor(tile.getColor());
                    g2d.fillRect(x * World.TILE_SIZE - camera.getX(),
                               y * World.TILE_SIZE - camera.getY(),
                               World.TILE_SIZE, World.TILE_SIZE);
                }
            }
        }
    }
    
    private void drawHealthBar(Graphics2D g2d) {
        int barWidth = 200;
        int barHeight = 20;
        int x = 10;
        int y = 120;
        
        // Background
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(x, y, barWidth, barHeight);
        
        // Health bar
        float healthPercent = (float) player.getHealth() / player.getMaxHealth();
        Color healthColor = healthPercent > 0.5f ? Color.GREEN : 
                           healthPercent > 0.25f ? Color.YELLOW : Color.RED;
        g2d.setColor(healthColor);
        g2d.fillRect(x, y, (int) (barWidth * healthPercent), barHeight);
        
        // Border
        g2d.setColor(Color.WHITE);
        g2d.drawRect(x, y, barWidth, barHeight);
        
        // Text
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        String healthText = player.getHealth() + "/" + player.getMaxHealth() + " HP";
        g2d.drawString(healthText, x + 5, y + 15);
        
        // Invulnerability indicator
        if (player.isInvulnerable()) {
            g2d.setColor(Color.CYAN);
            g2d.drawString("INVULNERABLE", x + barWidth + 10, y + 15);
        }
    }
    
    private void drawHotbar(Graphics2D g2d) {
        int hotbarY = WINDOW_HEIGHT - 70;
        int slotSize = 50;
        int spacing = 5;
        int startX = (WINDOW_WIDTH - (9 * slotSize + 8 * spacing)) / 2;
        
        ItemStack[] hotbar = player.getHotbar();
        
        for (int i = 0; i < 9; i++) {
            int slotX = startX + i * (slotSize + spacing);
            
            if (i == player.getSelectedSlot()) {
                g2d.setColor(new Color(255, 255, 0, 100));
            } else {
                g2d.setColor(new Color(128, 128, 128, 100));
            }
            g2d.fillRect(slotX, hotbarY, slotSize, slotSize);
            
            if (i == player.getSelectedSlot()) {
                g2d.setColor(Color.YELLOW);
                g2d.setStroke(new BasicStroke(2));
            } else {
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(1));
            }
            g2d.drawRect(slotX, hotbarY, slotSize, slotSize);
            
            ItemStack item = hotbar[i];
            if (item != null && !item.isEmpty()) {
                g2d.setColor(item.getItemType().getColor());
                g2d.fillRect(slotX + 8, hotbarY + 8, slotSize - 16, slotSize - 16);
                
                g2d.setColor(Color.BLACK);
                g2d.drawRect(slotX + 8, hotbarY + 8, slotSize - 16, slotSize - 16);
                
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                String quantityStr = String.valueOf(item.getQuantity());
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(quantityStr);
                g2d.drawString(quantityStr, 
                              slotX + slotSize - textWidth - 5, 
                              hotbarY + slotSize - 5);
            }
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            g2d.drawString(String.valueOf(i + 1), slotX + 2, hotbarY + 12);
        }
    }
    
    private void drawUI(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Health & Damage System Demo", 10, 20);
        g2d.drawString("Controls: A/D - Move, Space - Jump", 10, 35);
        g2d.drawString("Mouse: Left - Break, Right - Place", 10, 50);
        g2d.drawString("Keys 1-9: Select Hotbar Slot", 10, 65);
        g2d.drawString("Enemies: " + enemies.size(), 10, 80);
        g2d.drawString("Avoid red enemies and lava!", 10, 95);
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        int worldX = (e.getX() + camera.getX()) / World.TILE_SIZE;
        int worldY = (e.getY() + camera.getY()) / World.TILE_SIZE;
        
        if (e.getButton() == MouseEvent.BUTTON1) {
            TileType tileToBreak = world.getTile(worldX, worldY);
            if (tileToBreak != TileType.AIR) {
                world.setTile(worldX, worldY, TileType.AIR);
                player.collectItem(tileToBreak, 1);
                
                // Check if we hit an enemy
                for (Enemy enemy : enemies) {
                    if (Math.abs(enemy.getX() - worldX * World.TILE_SIZE) < World.TILE_SIZE &&
                        Math.abs(enemy.getY() - worldY * World.TILE_SIZE) < World.TILE_SIZE) {
                        enemy.takeDamage(25);
                        
                        // Add damage indicator
                        damageIndicators.add(new DamageIndicator(
                            enemy.getX(), enemy.getY() - 10, 25));
                        break;
                    }
                }
            }
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            ItemStack selectedItem = player.getSelectedItem();
            if (selectedItem != null && world.getTile(worldX, worldY) == TileType.AIR) {
                world.setTile(worldX, worldY, selectedItem.getItemType());
                player.useSelectedItem();
            }
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                leftPressed = true;
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                rightPressed = true;
                break;
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                spacePressed = true;
                break;
            case KeyEvent.VK_1: case KeyEvent.VK_2: case KeyEvent.VK_3:
            case KeyEvent.VK_4: case KeyEvent.VK_5: case KeyEvent.VK_6:
            case KeyEvent.VK_7: case KeyEvent.VK_8: case KeyEvent.VK_9:
                int slot = e.getKeyCode() - KeyEvent.VK_1;
                player.selectSlot(slot);
                break;
            default:
                break;
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                leftPressed = false;
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                rightPressed = false;
                break;
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                spacePressed = false;
                break;
            default:
                break;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Terraria Clone - Health & Damage System");
        HealthDamageGame game = new HealthDamageGame();
        
        frame.add(game);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        game.startGame();
        
        Runtime.getRuntime().addShutdownHook(new Thread(game::stopGame));
    }
}