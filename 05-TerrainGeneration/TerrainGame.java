import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Random;
import javax.swing.*;

/**
 * Lesson 5: Advanced Terrain Generation
 * 
 * This version features:
 * 1. Perlin noise-like terrain generation
 * 2. Multiple terrain layers
 * 3. Cave generation
 * 4. Different biomes
 * 5. Ore generation
 */

enum TileType {
    AIR(0, Color.BLACK, false),
    DIRT(1, new Color(139, 69, 19), true),
    STONE(2, Color.GRAY, true),
    GRASS(3, Color.GREEN, true),
    SAND(4, new Color(194, 178, 128), true),
    WATER(5, new Color(64, 164, 223), false),
    COAL_ORE(6, new Color(64, 64, 64), true),
    IRON_ORE(7, new Color(205, 127, 50), true),
    GOLD_ORE(8, new Color(255, 215, 0), true);
    
    private final int id;
    private final Color color;
    private final boolean solid;
    
    TileType(int id, Color color, boolean solid) {
        this.id = id;
        this.color = color;
        this.solid = solid;
    }
    
    public int getId() { return id; }
    public Color getColor() { return color; }
    public boolean isSolid() { return solid; }
}

class SimplexNoise {
    private final Random random;
    
    public SimplexNoise(long seed) {
        this.random = new Random(seed);
    }
    
    // Simple noise function (simplified version of Perlin noise)
    public double noise(double x, double y) {
        // This is a very basic noise implementation
        // In a real game, you'd want to use proper Perlin or Simplex noise
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
        random.setSeed(ix * 73856093 + iy * 19349663);
        double dx = x - ix;
        double dy = y - iy;
        
        double gradientX = random.nextGaussian();
        double gradientY = random.nextGaussian();
        
        return dx * gradientX + dy * gradientY;
    }
    
    private double interpolate(double a, double b, double t) {
        // Smooth interpolation
        t = t * t * (3.0 - 2.0 * t);
        return a + t * (b - a);
    }
}

class World {
    private static final int WORLD_WIDTH = 200;  // tiles
    private static final int WORLD_HEIGHT = 100; // tiles
    public static final int TILE_SIZE = 16;      // pixels
    
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
        // First pass: Generate basic terrain
        generateTerrain();
        
        // Second pass: Generate caves
        generateCaves();
        
        // Third pass: Place ores
        placeOres();
        
        // Fourth pass: Add water
        addWater();
    }
    
    private void generateTerrain() {
        for (int x = 0; x < WORLD_WIDTH; x++) {
            // Use noise to determine surface height
            double heightValue = heightNoise.noise(x * 0.01, 0) * 0.5 + 0.5;
            int surfaceHeight = (int) (20 + heightValue * 20); // Surface between y=20 and y=40
            
            for (int y = 0; y < WORLD_HEIGHT; y++) {
                if (y < surfaceHeight) {
                    tiles[x][y] = TileType.AIR;
                } else if (y < surfaceHeight + 1) {
                    // Determine biome based on x position
                    if (x < WORLD_WIDTH * 0.3 || x > WORLD_WIDTH * 0.7) {
                        tiles[x][y] = TileType.SAND; // Desert biome
                    } else {
                        tiles[x][y] = TileType.GRASS; // Grass biome
                    }
                } else if (y < surfaceHeight + 5) {
                    tiles[x][y] = TileType.DIRT;
                } else {
                    tiles[x][y] = TileType.STONE;
                }
            }
        }
    }
    
    private void generateCaves() {
        for (int x = 0; x < WORLD_WIDTH; x++) {
            for (int y = 25; y < WORLD_HEIGHT - 5; y++) { // Don't generate caves too close to surface or bottom
                // Use noise to determine if there should be a cave here
                double caveValue = caveNoise.noise(x * 0.05, y * 0.05);
                if (caveValue > 0.2) { // Threshold for cave generation
                    tiles[x][y] = TileType.AIR;
                }
            }
        }
    }
    
    private void placeOres() {
        for (int x = 0; x < WORLD_WIDTH; x++) {
            for (int y = 30; y < WORLD_HEIGHT; y++) {
                if (tiles[x][y] == TileType.STONE) {
                    random.setSeed(x * 12345 + y * 67890);
                    double oreChance = random.nextDouble();
                    
                    if (oreChance < 0.01) { // 1% chance for gold (deep)
                        if (y > 60) {
                            tiles[x][y] = TileType.GOLD_ORE;
                        }
                    } else if (oreChance < 0.03) { // 2% chance for iron
                        if (y > 40) {
                            tiles[x][y] = TileType.IRON_ORE;
                        }
                    } else if (oreChance < 0.07) { // 4% chance for coal
                        if (y > 30) {
                            tiles[x][y] = TileType.COAL_ORE;
                        }
                    }
                }
            }
        }
    }
    
    private void addWater() {
        // Add water to low-lying areas and caves
        for (int x = 0; x < WORLD_WIDTH; x++) {
            for (int y = WORLD_HEIGHT - 10; y < WORLD_HEIGHT; y++) {
                if (tiles[x][y] == TileType.AIR) {
                    // Check if this air pocket is below water level
                    boolean shouldHaveWater = true;
                    for (int checkY = y - 1; checkY >= 0; checkY--) {
                        if (tiles[x][checkY] != TileType.AIR) {
                            break;
                        }
                        if (checkY < WORLD_HEIGHT - 15) {
                            shouldHaveWater = false;
                            break;
                        }
                    }
                    if (shouldHaveWater) {
                        tiles[x][y] = TileType.WATER;
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
    
    public int getWorldWidth() { return WORLD_WIDTH; }
    public int getWorldHeight() { return WORLD_HEIGHT; }
}

class Camera {
    private int x, y;
    private int screenWidth, screenHeight;
    
    public Camera(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }
    
    public void update(int targetX, int targetY) {
        x = targetX - screenWidth / 2;
        y = targetY - screenHeight / 2;
        
        // Keep camera within world bounds
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x > World.TILE_SIZE * 200 - screenWidth) {
            x = World.TILE_SIZE * 200 - screenWidth;
        }
        if (y > World.TILE_SIZE * 100 - screenHeight) {
            y = World.TILE_SIZE * 100 - screenHeight;
        }
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
}

class Player {
    private float x, y;
    private float velocityX, velocityY;
    private int width, height;
    private boolean onGround;
    private World world;
    
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
    }
    
    public void update() {
        if (!onGround) {
            velocityY += GRAVITY;
        }
        
        moveHorizontal(velocityX);
        moveVertical(velocityY);
        
        velocityX = 0;
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
        
        g2d.setColor(Color.BLUE);
        g2d.fillRect(drawX, drawY, width, height);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(drawX, drawY, width, height);
    }
    
    public int getX() { return (int) x; }
    public int getY() { return (int) y; }
    public int getCenterX() { return (int) (x + width / 2); }
    public int getCenterY() { return (int) (y + height / 2); }
}

public class TerrainGame extends JPanel implements Runnable, KeyListener, MouseListener {
    
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final int TARGET_FPS = 60;
    private static final long TARGET_TIME = 1000000000 / TARGET_FPS;
    
    private boolean running = false;
    private Thread gameThread;
    private World world;
    private Player player;
    private Camera camera;
    
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean spacePressed = false;
    
    public TerrainGame() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        
        // Initialize with random seed
        long seed = System.currentTimeMillis();
        world = new World(seed);
        player = new Player(World.TILE_SIZE * 100, World.TILE_SIZE * 10, world);
        camera = new Camera(WINDOW_WIDTH, WINDOW_HEIGHT);
        
        System.out.println("World generated with seed: " + seed);
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
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        drawWorld(g2d);
        player.draw(g2d, camera);
        drawUI(g2d);
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
    
    private void drawUI(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Enhanced Terrain Generation", 10, 20);
        g2d.drawString("Controls: A/D - Move, Space - Jump", 10, 35);
        g2d.drawString("Mouse: Left - Break, Right - Place Dirt", 10, 50);
        g2d.drawString("Features: Caves, Ores, Biomes, Water", 10, 65);
        g2d.drawString("Player Tile: (" + (player.getX() / World.TILE_SIZE) + ", " + 
                      (player.getY() / World.TILE_SIZE) + ")", 10, 80);
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        int worldX = (e.getX() + camera.getX()) / World.TILE_SIZE;
        int worldY = (e.getY() + camera.getY()) / World.TILE_SIZE;
        
        if (e.getButton() == MouseEvent.BUTTON1) {
            world.setTile(worldX, worldY, TileType.AIR);
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            if (world.getTile(worldX, worldY) == TileType.AIR) {
                world.setTile(worldX, worldY, TileType.DIRT);
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
        JFrame frame = new JFrame("Enhanced Terrain Generation - Terraria-like");
        TerrainGame game = new TerrainGame();
        
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