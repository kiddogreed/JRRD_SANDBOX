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
 * Lesson 11: Crafting System
 * 
 * This builds on all previous lessons and adds:
 * 1. Crafting recipe system - combine items to create new items
 * 2. Workbench block - required for advanced crafting
 * 3. Crafting UI - 2x2 grid for creating items
 * 4. Basic crafting recipes (Workbench, Torches, Lamp, Stone Bricks)
 * 
 * New features:
 * - Press 'C' to open crafting interface
 * - Craft workbench from wood (stone blocks for now)
 * - Place workbench to unlock more recipes
 * - Recipe system that checks inventory and consumes ingredients
 * - Visual crafting grid with result preview
 * 
 * Controls:
 * - C: Open/Close crafting menu
 * - Mouse: Click on recipes to craft
 * - Near workbench: Access advanced recipes
 * 
 * All previous features from lessons 1-10 are included.
 */

enum TileType {
    AIR(0, Color.BLACK, false, false, 0),
    DIRT(1, new Color(139, 69, 19), true, false, 0),
    STONE(2, Color.GRAY, true, false, 0),
    GRASS(3, Color.GREEN, true, false, 0),
    SAND(4, new Color(194, 178, 128), true, false, 0),
    LAVA(5, new Color(255, 100, 0), false, true, 12), // Lava emits light
    COAL_ORE(6, new Color(64, 64, 64), true, false, 0),
    IRON_ORE(7, new Color(205, 127, 50), true, false, 0),
    GOLD_ORE(8, new Color(255, 215, 0), true, false, 0),
    TORCH(9, new Color(255, 200, 100), false, false, 14), // Torch block
    LAMP(10, new Color(255, 220, 150), true, false, 15); // NEW: Lamp - strongest light source!
    
    private final int id;
    private final Color color;
    private final boolean solid;
    private final boolean damaging;
    private final int lightLevel; // 0-15, how much light this block emits
    
    TileType(int id, Color color, boolean solid, boolean damaging, int lightLevel) {
        this.id = id;
        this.color = color;
        this.solid = solid;
        this.damaging = damaging;
        this.lightLevel = lightLevel;
    }
    
    public int getId() { return id; }
    public Color getColor() { return color; }
    public boolean isSolid() { return solid; }
    public boolean isDamaging() { return damaging; }
    public int getLightLevel() { return lightLevel; }
}

class ParallaxLayer {
    private Color baseColor;
    private float scrollSpeed; // How fast this layer moves relative to camera (0.0 = static, 1.0 = camera speed)
    private int[] heights; // Height variations for mountains/hills
    private int yOffset; // Vertical position
    private Random random;
    private long seed;
    
    public ParallaxLayer(Color baseColor, float scrollSpeed, int yOffset, long seed) {
        this.baseColor = baseColor;
        this.scrollSpeed = scrollSpeed;
        this.yOffset = yOffset;
        this.seed = seed;
        this.random = new Random(seed);
        this.heights = new int[200]; // Pre-generate height variations
        
        for (int i = 0; i < heights.length; i++) {
            heights[i] = 20 + random.nextInt(40);
        }
    }
    
    public void draw(Graphics2D g2d, Camera camera, int windowWidth, int windowHeight, TimeSystem timeSystem) {
        // Apply time-based color tinting
        Color skyColor = timeSystem.getSkyColor();
        float brightness = timeSystem.getAmbientLight();
        
        // Blend layer color with sky color based on time
        int r = (int) (baseColor.getRed() * brightness * 0.7 + skyColor.getRed() * 0.3);
        int g = (int) (baseColor.getGreen() * brightness * 0.7 + skyColor.getGreen() * 0.3);
        int b = (int) (baseColor.getBlue() * brightness * 0.7 + skyColor.getBlue() * 0.3);
        
        Color tintedColor = new Color(
            Math.min(255, Math.max(0, r)),
            Math.min(255, Math.max(0, g)),
            Math.min(255, Math.max(0, b))
        );
        
        g2d.setColor(tintedColor);
        
        // Calculate parallax offset
        int parallaxOffset = (int) (camera.getX() * scrollSpeed);
        
        // Draw silhouettes (mountains/hills)
        int baseY = windowHeight - yOffset;
        int segmentWidth = 40;
        
        for (int x = -segmentWidth; x < windowWidth + segmentWidth; x += segmentWidth) {
            int arrayIndex = ((x + parallaxOffset) / segmentWidth + 1000) % heights.length;
            if (arrayIndex < 0) arrayIndex += heights.length;
            
            int height = heights[arrayIndex];
            
            // Draw mountain/hill shape
            int[] xPoints = new int[4];
            int[] yPoints = new int[4];
            
            xPoints[0] = x - parallaxOffset % segmentWidth;
            yPoints[0] = baseY;
            
            xPoints[1] = x - parallaxOffset % segmentWidth;
            yPoints[1] = baseY - height;
            
            xPoints[2] = x + segmentWidth - parallaxOffset % segmentWidth;
            yPoints[2] = baseY - heights[(arrayIndex + 1) % heights.length];
            
            xPoints[3] = x + segmentWidth - parallaxOffset % segmentWidth;
            yPoints[3] = baseY;
            
            g2d.fillPolygon(xPoints, yPoints, 4);
        }
    }
}

class CelestialBodies {
    private Star[] stars;
    private Cloud[] clouds;
    private Random random;
    
    public CelestialBodies() {
        random = new Random(12345);
        
        // Create stars
        stars = new Star[100];
        for (int i = 0; i < stars.length; i++) {
            stars[i] = new Star(random);
        }
        
        // Create clouds
        clouds = new Cloud[8];
        for (int i = 0; i < clouds.length; i++) {
            clouds[i] = new Cloud(random);
        }
    }
    
    public void drawSun(Graphics2D g2d, TimeSystem timeSystem, int windowWidth, int windowHeight) {
        // Only draw sun during day
        if (!timeSystem.isNight()) {
            // Sun moves in an arc across the sky
            float dayProgress = (timeSystem.getTimeOfDay() - 0.3f) / 0.4f; // 0.3 to 0.7 is day
            dayProgress = Math.max(0, Math.min(1, dayProgress));
            
            int sunX = (int) (windowWidth * 0.2 + dayProgress * windowWidth * 0.6);
            int sunY = (int) (windowHeight * 0.25 - Math.sin(dayProgress * Math.PI) * windowHeight * 0.15);
            
            // Draw sun glow
            for (int i = 3; i > 0; i--) {
                g2d.setColor(new Color(255, 255, 200, 20 * i));
                g2d.fillOval(sunX - 25 * i, sunY - 25 * i, 50 * i, 50 * i);
            }
            
            // Draw sun
            g2d.setColor(new Color(255, 255, 100));
            g2d.fillOval(sunX - 25, sunY - 25, 50, 50);
            
            // Sun core
            g2d.setColor(new Color(255, 240, 150));
            g2d.fillOval(sunX - 20, sunY - 20, 40, 40);
        }
    }
    
    public void drawMoon(Graphics2D g2d, TimeSystem timeSystem, int windowWidth, int windowHeight) {
        // Only draw moon during night
        if (timeSystem.isNight()) {
            // Moon moves in an arc across the sky (opposite of sun)
            float nightProgress;
            if (timeSystem.getTimeOfDay() < 0.3f) {
                nightProgress = timeSystem.getTimeOfDay() / 0.3f;
            } else {
                nightProgress = (timeSystem.getTimeOfDay() - 0.7f) / 0.3f;
            }
            nightProgress = Math.max(0, Math.min(1, nightProgress));
            
            int moonX = (int) (windowWidth * 0.2 + nightProgress * windowWidth * 0.6);
            int moonY = (int) (windowHeight * 0.25 - Math.sin(nightProgress * Math.PI) * windowHeight * 0.15);
            
            // Draw moon glow
            g2d.setColor(new Color(200, 200, 255, 30));
            g2d.fillOval(moonX - 30, moonY - 30, 60, 60);
            
            // Draw moon
            g2d.setColor(new Color(240, 240, 255));
            g2d.fillOval(moonX - 20, moonY - 20, 40, 40);
            
            // Moon craters (simple details)
            g2d.setColor(new Color(220, 220, 240));
            g2d.fillOval(moonX - 8, moonY - 10, 6, 6);
            g2d.fillOval(moonX + 5, moonY - 5, 8, 8);
            g2d.fillOval(moonX - 10, moonY + 5, 5, 5);
        }
    }
    
    public void drawStars(Graphics2D g2d, TimeSystem timeSystem) {
        // Only draw stars during night
        if (timeSystem.isNight()) {
            float starOpacity = 1.0f;
            
            // Fade stars during twilight
            if (timeSystem.getTimeOfDay() < 0.15f) {
                starOpacity = timeSystem.getTimeOfDay() / 0.15f;
            } else if (timeSystem.getTimeOfDay() > 0.85f) {
                starOpacity = (1.0f - timeSystem.getTimeOfDay()) / 0.15f;
            }
            
            for (Star star : stars) {
                star.draw(g2d, starOpacity);
            }
        }
    }
    
    public void updateClouds(long deltaTime) {
        for (Cloud cloud : clouds) {
            cloud.update(deltaTime);
        }
    }
    
    public void drawClouds(Graphics2D g2d, int windowWidth) {
        for (Cloud cloud : clouds) {
            cloud.draw(g2d, windowWidth);
        }
    }
}

class Star {
    private int x, y;
    private float brightness;
    private float twinkleSpeed;
    private float twinklePhase;
    
    public Star(Random random) {
        this.x = random.nextInt(800);
        this.y = random.nextInt(300);
        this.brightness = 0.5f + random.nextFloat() * 0.5f;
        this.twinkleSpeed = 0.5f + random.nextFloat() * 1.5f;
        this.twinklePhase = random.nextFloat() * (float)Math.PI * 2;
    }
    
    public void draw(Graphics2D g2d, float opacity) {
        // Calculate twinkling effect
        twinklePhase += 0.05f * twinkleSpeed;
        float twinkle = (float)(Math.sin(twinklePhase) * 0.3 + 0.7);
        
        int alpha = (int)(brightness * twinkle * opacity * 255);
        alpha = Math.max(0, Math.min(255, alpha));
        
        g2d.setColor(new Color(255, 255, 255, alpha));
        g2d.fillRect(x, y, 2, 2);
    }
}

class Cloud {
    private float x;
    private float y;
    private float speed;
    private int width;
    private int height;
    
    public Cloud(Random random) {
        this.x = random.nextInt(800);
        this.y = 50 + random.nextInt(150);
        this.speed = 5 + random.nextFloat() * 10;
        this.width = 60 + random.nextInt(80);
        this.height = 30 + random.nextInt(30);
    }
    
    public void update(long deltaTime) {
        x += speed * (deltaTime / 1000.0f);
    }
    
    public void draw(Graphics2D g2d, int windowWidth) {
        // Wrap around
        float drawX = x % (windowWidth + width) - width;
        
        g2d.setColor(new Color(255, 255, 255, 60));
        
        // Draw simple cloud shape (3 overlapping ovals)
        g2d.fillOval((int)drawX, (int)y, width / 2, height);
        g2d.fillOval((int)drawX + width / 3, (int)y - height / 4, width / 2, height);
        g2d.fillOval((int)drawX + width / 2, (int)y, width / 2, height);
    }
}

class TimeSystem {
    private float timeOfDay = 0.5f; // 0.0 = midnight, 0.5 = noon, 1.0 = next midnight
    private static final float DAY_LENGTH_MS = 120000f; // 2 minutes = full day/night cycle
    
    public void update(long deltaTime) {
        timeOfDay += deltaTime / DAY_LENGTH_MS;
        if (timeOfDay >= 1.0f) {
            timeOfDay = 0.0f; // Reset to start new day
        }
    }
    
    public Color getSkyColor() {
        // Calculate sky color based on time of day (longer nights: 0.7-0.3)
        if (timeOfDay >= 0.0f && timeOfDay < 0.3f) {
            // Night to dawn (very dark to orange) - extended night period
            float t = timeOfDay / 0.3f;
            return interpolateColor(new Color(5, 5, 15), new Color(255, 100, 50), t);
        } else if (timeOfDay >= 0.3f && timeOfDay < 0.4f) {
            // Dawn to day (orange to light blue) - shorter dawn
            float t = (timeOfDay - 0.3f) / 0.1f;
            return interpolateColor(new Color(255, 100, 50), new Color(135, 206, 235), t);
        } else if (timeOfDay >= 0.4f && timeOfDay < 0.6f) {
            // Day (light blue) - shorter day period
            return new Color(135, 206, 235);
        } else if (timeOfDay >= 0.6f && timeOfDay < 0.7f) {
            // Day to dusk (light blue to orange) - shorter dusk
            float t = (timeOfDay - 0.6f) / 0.1f;
            return interpolateColor(new Color(135, 206, 235), new Color(255, 100, 50), t);
        } else {
            // Dusk to night (orange to very dark) - extended night period
            float t = (timeOfDay - 0.7f) / 0.3f;
            return interpolateColor(new Color(255, 100, 50), new Color(5, 5, 15), t);
        }
    }
    
    private Color interpolateColor(Color c1, Color c2, float t) {
        t = Math.max(0, Math.min(1, t)); // Clamp t between 0 and 1
        int r = (int) (c1.getRed() + t * (c2.getRed() - c1.getRed()));
        int g = (int) (c1.getGreen() + t * (c2.getGreen() - c1.getGreen()));
        int b = (int) (c1.getBlue() + t * (c2.getBlue() - c1.getBlue()));
        return new Color(r, g, b);
    }
    
    public float getAmbientLight() {
        // Calculate ambient light level (0.0 = completely dark, 1.0 = full daylight)
        // Adjusted for longer nights: day from 0.35 to 0.65
        if (timeOfDay >= 0.35f && timeOfDay < 0.65f) {
            return 1.0f; // Full daylight - shorter day period
        } else if (timeOfDay >= 0.15f && timeOfDay < 0.35f) {
            // Dawn transition - longer transition
            float t = (timeOfDay - 0.15f) / 0.2f;
            return 0.1f + t * 0.9f;
        } else if (timeOfDay >= 0.65f && timeOfDay < 0.85f) {
            // Dusk transition - longer transition
            float t = (timeOfDay - 0.65f) / 0.2f;
            return 1.0f - t * 0.95f;
        } else {
            return 0.05f; // Night (very dark for scary atmosphere) - longer night period
        }
    }
    
    public boolean isNight() {
        return timeOfDay < 0.3f || timeOfDay > 0.7f; // Extended night period
    }
    
    public String getTimeString() {
        int hour = (int) (timeOfDay * 24);
        int minute = (int) ((timeOfDay * 24 - hour) * 60);
        return String.format("%02d:%02d", hour, minute);
    }
    
    public float getTimeOfDay() { return timeOfDay; }
}

class LightingSystem {
    private int[][] lightLevels;
    private int worldWidth, worldHeight;
    private World world;
    
    public LightingSystem(int worldWidth, int worldHeight, World world) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.world = world;
        this.lightLevels = new int[worldWidth][worldHeight];
    }
    
    public void calculateLighting(float ambientLight) {
        // Reset all light levels
        for (int x = 0; x < worldWidth; x++) {
            for (int y = 0; y < worldHeight; y++) {
                lightLevels[x][y] = 0;
            }
        }
        
        // Add sunlight from the top during day
        if (ambientLight > 0.3f) {
            addSunlight((int) (15 * ambientLight));
        }
        
        // Add light from light-emitting blocks
        addBlockLight();
        
        // Propagate light
        propagateLight();
    }
    
    private void addSunlight(int sunlightLevel) {
        for (int x = 0; x < worldWidth; x++) {
            int currentLight = sunlightLevel;
            for (int y = 0; y < worldHeight; y++) {
                if (world.getTile(x, y) == TileType.AIR) {
                    lightLevels[x][y] = Math.max(lightLevels[x][y], currentLight);
                    currentLight = Math.max(0, currentLight - 1);
                } else {
                    break; // Stop sunlight when hitting solid block
                }
            }
        }
    }
    
    private void addBlockLight() {
        for (int x = 0; x < worldWidth; x++) {
            for (int y = 0; y < worldHeight; y++) {
                TileType tile = world.getTile(x, y);
                if (tile.getLightLevel() > 0) {
                    lightLevels[x][y] = tile.getLightLevel();
                }
            }
        }
    }
    
    private void propagateLight() {
        // Simple light propagation (multiple passes for better quality)
        for (int pass = 0; pass < 3; pass++) {
            int[][] newLightLevels = new int[worldWidth][worldHeight];
            
            for (int x = 0; x < worldWidth; x++) {
                for (int y = 0; y < worldHeight; y++) {
                    newLightLevels[x][y] = lightLevels[x][y];
                    
                    // Check neighboring cells and propagate light
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            if (dx == 0 && dy == 0) continue;
                            
                            int nx = x + dx;
                            int ny = y + dy;
                            
                            if (nx >= 0 && nx < worldWidth && ny >= 0 && ny < worldHeight) {
                                if (world.getTile(nx, ny) == TileType.AIR && lightLevels[nx][ny] > 1) {
                                    int propagatedLight = lightLevels[nx][ny] - 1;
                                    newLightLevels[x][y] = Math.max(newLightLevels[x][y], propagatedLight);
                                }
                            }
                        }
                    }
                }
            }
            
            lightLevels = newLightLevels;
        }
    }
    
    public int getLightLevel(int x, int y) {
        if (x < 0 || x >= worldWidth || y < 0 || y >= worldHeight) {
            return 0;
        }
        return lightLevels[x][y];
    }
    
    public Color applyLighting(Color baseColor, int lightLevel, float ambientLight) {
        // Make night much darker - reduced ambient light contribution
        float totalLight = Math.max(lightLevel / 15.0f, ambientLight * 0.15f);
        totalLight = Math.min(1.0f, totalLight);
        
        int r = (int) (baseColor.getRed() * totalLight);
        int g = (int) (baseColor.getGreen() * totalLight);
        int b = (int) (baseColor.getBlue() * totalLight);
        
        return new Color(r, g, b);
    }
    
    // NEW: Apply warm lamp glow effect
    public Color applyLampGlow(Color baseColor, int lightLevel, float ambientLight) {
        float totalLight = Math.max(lightLevel / 15.0f, ambientLight * 0.15f);
        totalLight = Math.min(1.0f, totalLight);
        
        // Add warm orange tint for lamp light
        float warmth = lightLevel / 15.0f * 0.3f; // 30% warm tint at max light
        
        int r = (int) (baseColor.getRed() * totalLight * (1.0f + warmth));
        int g = (int) (baseColor.getGreen() * totalLight * (1.0f + warmth * 0.7f));
        int b = (int) (baseColor.getBlue() * totalLight);
        
        r = Math.min(255, r);
        g = Math.min(255, g);
        b = Math.min(255, b);
        
        return new Color(r, g, b);
    }
    
    // NEW: Check if a block creates shadow (is solid and blocks light)
    public boolean isShadowCaster(int x, int y) {
        if (x < 0 || x >= worldWidth || y < 0 || y >= worldHeight) {
            return false;
        }
        TileType tile = world.getTile(x, y);
        return tile.isSolid() && tile != TileType.LAMP; // Lamps don't cast shadows
    }
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
            case TORCH:
                return 16;
            case LAMP: // NEW: Lamps stack to 8
                return 8;
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
    private static final long DURATION = 2000;
    
    public DamageIndicator(int x, int y, int damage) {
        this.x = x;
        this.y = y;
        this.damage = damage;
        this.creationTime = System.currentTimeMillis();
    }
    
    public void update() {
        y -= 1;
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
    private static final long ATTACK_COOLDOWN = 2000;
    
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
                    if (Math.random() < 0.01) {
                        velocityX = (Math.random() < 0.5) ? -MOVE_SPEED : MOVE_SPEED;
                    }
                    if (Math.random() < 0.005) {
                        velocityX = 0;
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
                    if (target.getX() < x) {
                        velocityX = -MOVE_SPEED;
                    } else {
                        velocityX = MOVE_SPEED;
                    }
                    
                    if (checkWallInDirection(velocityX > 0 ? 1 : -1) && onGround) {
                        velocityY = -8.0f;
                    }
                }
                break;
                
            case ATTACK:
                if (distToPlayer > ATTACK_RANGE) {
                    state = AIState.CHASE;
                } else {
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
    
    public void draw(Graphics2D g2d, Camera camera, LightingSystem lighting, float ambientLight) {
        int drawX = (int) (x - camera.getX());
        int drawY = (int) (y - camera.getY());
        
        // Get lighting for enemy position
        int lightLevel = lighting.getLightLevel((int) (x / World.TILE_SIZE), (int) (y / World.TILE_SIZE));
        Color enemyColor = lighting.applyLighting(Color.RED, lightLevel, ambientLight);
        
        // Enemy body
        g2d.setColor(enemyColor);
        g2d.fillRect(drawX, drawY, width, height);
        
        // Enemy outline
        Color outlineColor = lighting.applyLighting(Color.DARK_GRAY, lightLevel, ambientLight);
        g2d.setColor(outlineColor);
        g2d.drawRect(drawX, drawY, width, height);
        
        // Simple eyes (always visible for gameplay)
        g2d.setColor(Color.WHITE);
        g2d.fillOval(drawX + 3, drawY + 3, 3, 3);
        g2d.fillOval(drawX + 10, drawY + 3, 3, 3);
        
        // Health bar
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
        placeLava();
        placeTorches(); // New: Add some torches for lighting
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
    
    private void placeTorches() {
        // Place some torches in caves for initial lighting
        for (int x = 1; x < WORLD_WIDTH - 1; x += 20) {
            for (int y = 25; y < WORLD_HEIGHT - 10; y += 15) {
                if (tiles[x][y] == TileType.AIR && 
                    (tiles[x][y + 1] != TileType.AIR || tiles[x - 1][y] != TileType.AIR || tiles[x + 1][y] != TileType.AIR)) {
                    random.setSeed(x * 11111L + y * 22222L);
                    if (random.nextDouble() < 0.3) {
                        tiles[x][y] = TileType.TORCH;
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
    private static final long DAMAGE_COOLDOWN = 1000;
    private static final long HEALTH_REGEN_INTERVAL = 5000;
    private static final int REGEN_AMOUNT = 5;
    
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
        
        // Give starting items including torches and lamps
        collectItem(TileType.DIRT, 20);
        collectItem(TileType.STONE, 15);
        collectItem(TileType.TORCH, 10); // Give some torches to start
        collectItem(TileType.LAMP, 5); // NEW: Give 5 lamps to start!
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
        
        if (health < maxHealth && currentTime - lastRegenTime > HEALTH_REGEN_INTERVAL) {
            heal(REGEN_AMOUNT);
            lastRegenTime = currentTime;
        }
        
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
        int tileX = (int) ((x + (double) width / 2) / World.TILE_SIZE);
        int tileY = (int) ((y + (double) height / 2) / World.TILE_SIZE);
        
        if (world.isDamaging(tileX, tileY)) {
            takeDamage(10);
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
            
            return true;
        }
        return false;
    }
    
    public void heal(int healAmount) {
        health = Math.min(maxHealth, health + healAmount);
        lastRegenTime = System.currentTimeMillis();
    }
    
    private void die() {
        health = maxHealth;
        x = (float) World.TILE_SIZE * 75;
        y = (float) World.TILE_SIZE * 10;
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
    
    public void draw(Graphics2D g2d, Camera camera, LightingSystem lighting, float ambientLight) {
        int drawX = (int) (x - camera.getX());
        int drawY = (int) (y - camera.getY());
        
        // Skip drawing during invulnerability flash
        if (isInvulnerable && invulnerabilityFlashTimer % 10 < 5) {
            return;
        }
        
        // Apply lighting to player
        int lightLevel = lighting.getLightLevel((int) (x / World.TILE_SIZE), (int) (y / World.TILE_SIZE));
        Color playerColor = lighting.applyLighting(Color.BLUE, lightLevel, ambientLight);
        Color outlineColor = lighting.applyLighting(Color.WHITE, lightLevel, ambientLight);
        
        g2d.setColor(playerColor);
        g2d.fillRect(drawX, drawY, width, height);
        g2d.setColor(outlineColor);
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

public class LightingEffectsGame extends JPanel implements Runnable, KeyListener, MouseListener {
    
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
    private TimeSystem timeSystem;
    private LightingSystem lightingSystem;
    private ParallaxLayer[] parallaxLayers;
    private CelestialBodies celestialBodies;
    
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean spacePressed = false;
    
    private long lastFrameTime = System.currentTimeMillis();
    
    public LightingEffectsGame() {
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
        timeSystem = new TimeSystem();
        lightingSystem = new LightingSystem(world.getWorldWidth(), world.getWorldHeight(), world);
        celestialBodies = new CelestialBodies();
        
        // Initialize parallax background layers (farthest to closest)
        parallaxLayers = new ParallaxLayer[] {
            new ParallaxLayer(new Color(40, 60, 100), 0.1f, 400, seed + 1),      // Far mountains
            new ParallaxLayer(new Color(60, 80, 120), 0.25f, 350, seed + 2),     // Mid mountains  
            new ParallaxLayer(new Color(80, 100, 140), 0.5f, 300, seed + 3),     // Near hills
            new ParallaxLayer(new Color(100, 120, 160), 0.75f, 250, seed + 4)    // Closest hills
        };
        
        spawnEnemies();
        
        System.out.println("Lighting Effects System initialized!");
        System.out.println("Controls: A/D - Move, Space - Jump, 1-9 - Select slot");
        System.out.println("NEW: LAMP blocks emit warm glowing light!");
        System.out.println("Place lamps to light up dark caves and survive the night!");
    }
    
    private void spawnEnemies() {
        Random random = new Random();
        for (int i = 0; i < 3; i++) { // Start with fewer enemies
            float enemyX = random.nextInt(world.getWorldWidth() * World.TILE_SIZE);
            float enemyY = World.TILE_SIZE * 15;
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
            long currentTime = System.currentTimeMillis();
            long deltaTime = currentTime - lastFrameTime;
            lastFrameTime = currentTime;
            
            update(deltaTime);
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
    
    private void update(long deltaTime) {
        // Update time system
        timeSystem.update(deltaTime);
        celestialBodies.updateClouds(deltaTime);
        
        // Update lighting
        lightingSystem.calculateLighting(timeSystem.getAmbientLight());
        
        // Spawn more enemies at night
        if (timeSystem.isNight() && enemies.size() < 8 && Math.random() < 0.001) {
            Random random = new Random();
            float enemyX = random.nextInt(world.getWorldWidth() * World.TILE_SIZE);
            float enemyY = World.TILE_SIZE * 15;
            enemies.add(new Enemy(enemyX, enemyY, world, player));
        }
        
        // Update game objects
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
        
        // Draw sky with dynamic color
        g2d.setColor(timeSystem.getSkyColor());
        g2d.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        // Draw celestial bodies (stars must be first, before parallax layers)
        celestialBodies.drawStars(g2d, timeSystem);
        
        // Draw parallax background layers (before world for depth)
        for (ParallaxLayer layer : parallaxLayers) {
            layer.draw(g2d, camera, WINDOW_WIDTH, WINDOW_HEIGHT, timeSystem);
        }
        
        // Draw celestial bodies (moon, sun, clouds on top of parallax)
        celestialBodies.drawMoon(g2d, timeSystem, WINDOW_WIDTH, WINDOW_HEIGHT);
        celestialBodies.drawSun(g2d, timeSystem, WINDOW_WIDTH, WINDOW_HEIGHT);
        celestialBodies.drawClouds(g2d, WINDOW_WIDTH);
        
        drawWorld(g2d);
        
        // Draw enemies with lighting
        for (Enemy enemy : enemies) {
            enemy.draw(g2d, camera, lightingSystem, timeSystem.getAmbientLight());
        }
        
        player.draw(g2d, camera, lightingSystem, timeSystem.getAmbientLight());
        
        // Draw damage indicators
        for (DamageIndicator indicator : damageIndicators) {
            indicator.draw(g2d, camera);
        }
        
        drawUI(g2d);
        drawHotbar(g2d);
        drawHealthBar(g2d);
        drawTimeInfo(g2d);
    }
    
    private void drawWorld(Graphics2D g2d) {
        int startX = Math.max(0, camera.getX() / World.TILE_SIZE);
        int endX = Math.min(world.getWorldWidth(), 
                           (camera.getX() + WINDOW_WIDTH) / World.TILE_SIZE + 1);
        int startY = Math.max(0, camera.getY() / World.TILE_SIZE);
        int endY = Math.min(world.getWorldHeight(), 
                           (camera.getY() + WINDOW_HEIGHT) / World.TILE_SIZE + 1);
        
        // First pass: Draw tiles with lighting
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                TileType tile = world.getTile(x, y);
                if (tile != TileType.AIR) {
                    int lightLevel = lightingSystem.getLightLevel(x, y);
                    
                    // NEW: Use warm lamp glow for better lighting effect
                    Color tileColor;
                    if (tile == TileType.LAMP || (lightLevel > 10 && timeSystem.isNight())) {
                        tileColor = lightingSystem.applyLampGlow(tile.getColor(), lightLevel, timeSystem.getAmbientLight());
                    } else {
                        tileColor = lightingSystem.applyLighting(tile.getColor(), lightLevel, timeSystem.getAmbientLight());
                    }
                    
                    g2d.setColor(tileColor);
                    g2d.fillRect(x * World.TILE_SIZE - camera.getX(),
                               y * World.TILE_SIZE - camera.getY(),
                               World.TILE_SIZE, World.TILE_SIZE);
                }
            }
        }
        
        // NEW: Second pass: Draw lamp glow effects
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                TileType tile = world.getTile(x, y);
                if (tile == TileType.LAMP) {
                    drawLampGlow(g2d, x, y);
                }
            }
        }
        
        // NEW: Third pass: Draw simple shadows
        if (timeSystem.isNight()) {
            drawSimpleShadows(g2d, startX, endX, startY, endY);
        }
    }
    
    // NEW: Draw glowing effect around lamps
    private void drawLampGlow(Graphics2D g2d, int tileX, int tileY) {
        int screenX = tileX * World.TILE_SIZE - camera.getX();
        int screenY = tileY * World.TILE_SIZE - camera.getY();
        
        // Draw multiple layers of glow for soft effect
        int glowSize = 48; // 3 tiles
        for (int i = 3; i > 0; i--) {
            int alpha = (int) (40 / i); // Fade out as we go further
            g2d.setColor(new Color(255, 220, 150, alpha));
            int size = glowSize * i / 3;
            g2d.fillOval(screenX - (size - World.TILE_SIZE) / 2, 
                        screenY - (size - World.TILE_SIZE) / 2,
                        size, size);
        }
        
        // Bright center
        g2d.setColor(new Color(255, 240, 200, 80));
        g2d.fillRect(screenX, screenY, World.TILE_SIZE, World.TILE_SIZE);
    }
    
    // NEW: Draw simple shadows from solid blocks
    private void drawSimpleShadows(Graphics2D g2d, int startX, int endX, int startY, int endY) {
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                // Check if this is a solid block that can cast shadow
                if (lightingSystem.isShadowCaster(x, y)) {
                    int lightLevel = lightingSystem.getLightLevel(x, y);
                    
                    // Only cast shadow if in dark area
                    if (lightLevel < 8) {
                        int screenX = x * World.TILE_SIZE - camera.getX();
                        int screenY = y * World.TILE_SIZE - camera.getY();
                        
                        // Calculate shadow darkness based on light level
                        int shadowAlpha = (int) ((8 - lightLevel) / 8.0f * 60);
                        g2d.setColor(new Color(0, 0, 0, shadowAlpha));
                        g2d.fillRect(screenX, screenY, World.TILE_SIZE, World.TILE_SIZE);
                        
                        // Slight gradient on bottom edge for depth
                        g2d.setColor(new Color(0, 0, 0, shadowAlpha / 2));
                        g2d.fillRect(screenX, screenY + World.TILE_SIZE - 2, World.TILE_SIZE, 2);
                    }
                }
            }
        }
    }
    
    private void drawTimeInfo(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        
        String timeStr = "Time: " + timeSystem.getTimeString();
        String phaseStr = timeSystem.isNight() ? "NIGHT" : "DAY";
        String lightStr = "Ambient Light: " + (int) (timeSystem.getAmbientLight() * 100) + "%";
        
        g2d.drawString(timeStr, WINDOW_WIDTH - 150, 20);
        g2d.drawString(phaseStr, WINDOW_WIDTH - 150, 35);
        g2d.drawString(lightStr, WINDOW_WIDTH - 150, 50);
        
        // Night warning
        if (timeSystem.isNight()) {
            g2d.setColor(Color.RED);
            g2d.drawString("MORE ENEMIES SPAWN AT NIGHT!", WINDOW_WIDTH - 200, 70);
        }
    }
    
    private void drawHealthBar(Graphics2D g2d) {
        int barWidth = 200;
        int barHeight = 20;
        int x = 10;
        int y = 120;
        
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(x, y, barWidth, barHeight);
        
        float healthPercent = (float) player.getHealth() / player.getMaxHealth();
        Color healthColor = healthPercent > 0.5f ? Color.GREEN : 
                           healthPercent > 0.25f ? Color.YELLOW : Color.RED;
        g2d.setColor(healthColor);
        g2d.fillRect(x, y, (int) (barWidth * healthPercent), barHeight);
        
        g2d.setColor(Color.WHITE);
        g2d.drawRect(x, y, barWidth, barHeight);
        
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        String healthText = player.getHealth() + "/" + player.getMaxHealth() + " HP";
        g2d.drawString(healthText, x + 5, y + 15);
        
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
        g2d.drawString("Lighting Effects Demo - Lesson 10", 10, 20);
        g2d.drawString("Controls: A/D - Move, Space - Jump", 10, 35);
        g2d.drawString("Mouse: Left - Break, Right - Place", 10, 50);
        g2d.drawString("Keys 1-9: Select Hotbar Slot", 10, 65);
        g2d.drawString("Enemies: " + enemies.size(), 10, 80);
        g2d.drawString("NEW: Place LAMPS for glowing light!", 10, 95);
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
        JFrame frame = new JFrame("Terraria Clone - Lighting Effects");
        LightingEffectsGame game = new LightingEffectsGame();
        
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