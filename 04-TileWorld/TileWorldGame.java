import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.*;

/**
 * Lesson 4: Tile-Based World System
 * 
 * This is the foundation for a Terraria-like game!
 * 
 * Features:
 * 1. Tile-based world representation
 * 2. Different tile types (air, dirt, stone, grass)
 * 3. Player movement with tile collision
 * 4. Camera system that follows the player
 * 5. Block breaking and placing with mouse
 */

enum TileType {
    AIR(0, Color.BLACK, false),
    DIRT(1, new Color(139, 69, 19), true),
    STONE(2, Color.GRAY, true),
    GRASS(3, Color.GREEN, true);
    
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

class World {
    private static final int WORLD_WIDTH = 100;  // tiles
    private static final int WORLD_HEIGHT = 60;  // tiles
    public static final int TILE_SIZE = 16;      // pixels
    
    private TileType[][] tiles;
    
    public World() {
        tiles = new TileType[WORLD_WIDTH][WORLD_HEIGHT];
        generateWorld();
    }
    
    private void generateWorld() {
        // Simple world generation
        for (int x = 0; x < WORLD_WIDTH; x++) {
            for (int y = 0; y < WORLD_HEIGHT; y++) {
                if (y < 20) {
                    tiles[x][y] = TileType.AIR; // Sky
                } else if (y < 25) {
                    tiles[x][y] = TileType.GRASS; // Surface
                } else if (y < 45) {
                    tiles[x][y] = TileType.DIRT; // Dirt layer
                } else {
                    tiles[x][y] = TileType.STONE; // Deep stone
                }
            }
        }
    }
    
    public TileType getTile(int x, int y) {
        if (x < 0 || x >= WORLD_WIDTH || y < 0 || y >= WORLD_HEIGHT) {
            return TileType.STONE; // Solid boundary
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
        this.x = 0;
        this.y = 0;
    }
    
    public void update(int targetX, int targetY) {
        // Center camera on target
        x = targetX - screenWidth / 2;
        y = targetY - screenHeight / 2;
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
        // Apply gravity
        if (!onGround) {
            velocityY += GRAVITY;
        }
        
        // Apply movement
        moveHorizontal(velocityX);
        moveVertical(velocityY);
        
        // Reset horizontal velocity
        velocityX = 0;
    }
    
    private void moveHorizontal(float deltaX) {
        x += deltaX;
        
        // Check horizontal collisions
        if (checkCollision()) {
            // Move back
            x -= deltaX;
        }
    }
    
    private void moveVertical(float deltaY) {
        y += deltaY;
        
        // Check vertical collisions
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
        // Check all four corners of player
        int left = (int) (x / World.TILE_SIZE);
        int right = (int) ((x + width) / World.TILE_SIZE);
        int top = (int) (y / World.TILE_SIZE);
        int bottom = (int) ((y + height) / World.TILE_SIZE);
        
        return world.isSolid(left, top) || world.isSolid(right, top) ||
               world.isSolid(left, bottom) || world.isSolid(right, bottom);
    }
    
    public void moveLeft() {
        velocityX = -MOVE_SPEED;
    }
    
    public void moveRight() {
        velocityX = MOVE_SPEED;
    }
    
    public void jump() {
        if (onGround) {
            velocityY = JUMP_STRENGTH;
            onGround = false;
        }
    }
    
    public void draw(Graphics2D g2d, Camera camera) {
        int drawX = (int) (x - camera.getX());
        int drawY = (int) (y - camera.getY());
        
        // Draw player
        g2d.setColor(Color.BLUE);
        g2d.fillRect(drawX, drawY, width, height);
        
        // Draw outline
        g2d.setColor(Color.WHITE);
        g2d.drawRect(drawX, drawY, width, height);
    }
    
    public int getX() { return (int) x; }
    public int getY() { return (int) y; }
    public int getCenterX() { return (int) (x + width / 2); }
    public int getCenterY() { return (int) (y + height / 2); }
}

public class TileWorldGame extends JPanel implements Runnable, KeyListener, MouseListener {
    
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final int TARGET_FPS = 60;
    private static final long TARGET_TIME = 1000000000 / TARGET_FPS;
    
    private boolean running = false;
    private Thread gameThread;
    private World world;
    private Player player;
    private Camera camera;
    
    // Key states
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean spacePressed = false;
    
    public TileWorldGame() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        
        // Initialize game objects
        world = new World();
        player = new Player(400, 200, world); // Start in the air
        camera = new Camera(WINDOW_WIDTH, WINDOW_HEIGHT);
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
        // Handle input
        if (leftPressed) {
            player.moveLeft();
        }
        if (rightPressed) {
            player.moveRight();
        }
        if (spacePressed) {
            player.jump();
        }
        
        // Update game objects
        player.update();
        camera.update(player.getCenterX(), player.getCenterY());
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Draw world tiles
        drawWorld(g2d);
        
        // Draw player
        player.draw(g2d, camera);
        
        // Draw UI
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
        g2d.drawString("Controls: A/D - Move, Space - Jump", 10, 20);
        g2d.drawString("Mouse: Left Click - Break Block, Right Click - Place Dirt", 10, 35);
        g2d.drawString("Player: (" + (player.getX() / World.TILE_SIZE) + ", " + 
                      (player.getY() / World.TILE_SIZE) + ")", 10, 50);
        g2d.drawString("Camera: (" + camera.getX() + ", " + camera.getY() + ")", 10, 65);
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        // Convert screen coordinates to world coordinates
        int worldX = (e.getX() + camera.getX()) / World.TILE_SIZE;
        int worldY = (e.getY() + camera.getY()) / World.TILE_SIZE;
        
        if (e.getButton() == MouseEvent.BUTTON1) {
            // Left click - break block
            world.setTile(worldX, worldY, TileType.AIR);
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            // Right click - place dirt block
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
        JFrame frame = new JFrame("Tile World Game - Terraria-like Foundation");
        TileWorldGame game = new TileWorldGame();
        
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