import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Random;
import javax.swing.*;

/**
 * Lesson 6: Inventory System Implementation
 * 
 * This builds on the TerrainGame and adds:
 * 1. Complete inventory system with hotbar
 * 2. Item collection when breaking blocks
 * 3. Block placement from inventory
 * 4. Visual hotbar with item counts
 * 5. Hotbar selection with number keys
 */

enum TileType {
    AIR(0, Color.BLACK, false),
    DIRT(1, new Color(139, 69, 19), true),
    STONE(2, Color.GRAY, true),
    GRASS(3, Color.GREEN, true),
    SAND(4, new Color(194, 178, 128), true),
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
                return 64; // Most blocks stack to 64
            case COAL_ORE:
            case IRON_ORE:
            case GOLD_ORE:
                return 32; // Ores stack to 32
            default:
                return 1;  // Special items don't stack
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
    
    // Getters
    public TileType getItemType() { return itemType; }
    public int getQuantity() { return quantity; }
    public boolean isEmpty() { return quantity <= 0; }
    public int getMaxStackSize() { return maxStackSize; }
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
        random.setSeed((long) ix * 73856093L + (long) iy * 19349663L);
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
                    random.setSeed((long) x * 12345L + (long) y * 67890L);
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
    private ItemStack[] inventory = new ItemStack[36]; // 4 rows of 9
    private ItemStack[] hotbar = new ItemStack[9];     // Quick access
    private int selectedSlot = 0;
    
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
        
        // Initialize hotbar to reference first 9 inventory slots
        for (int i = 0; i < 9; i++) {
            hotbar[i] = inventory[i];
        }
        
        // Give player some starting items for testing
        collectItem(TileType.DIRT, 20);
        collectItem(TileType.STONE, 15);
    }
    
    public void collectItem(TileType itemType, int quantity) {
        // First try to add to existing stacks
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
        
        // Add to empty slots
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
        // Keep hotbar synchronized with first 9 inventory slots
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
    public int getCenterX() { return (int) (x + (double) width / 2); }
    public int getCenterY() { return (int) (y + (double) height / 2); }
    public int getSelectedSlot() { return selectedSlot; }
    public ItemStack[] getHotbar() { return hotbar; }
    public ItemStack[] getInventory() { return inventory; }
}

public class InventoryGame extends JPanel implements Runnable, KeyListener, MouseListener {
    
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
    
    public InventoryGame() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        
        long seed = System.currentTimeMillis();
        world = new World(seed);
        player = new Player(World.TILE_SIZE * 75, World.TILE_SIZE * 10, world);
        camera = new Camera(WINDOW_WIDTH, WINDOW_HEIGHT);
        
        System.out.println("Inventory system initialized!");
        System.out.println("Use number keys 1-9 to select hotbar slots");
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
        drawHotbar(g2d);
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
    
    private void drawHotbar(Graphics2D g2d) {
        int hotbarY = WINDOW_HEIGHT - 70;
        int slotSize = 50;
        int spacing = 5;
        int startX = (WINDOW_WIDTH - (9 * slotSize + 8 * spacing)) / 2;
        
        ItemStack[] hotbar = player.getHotbar();
        
        for (int i = 0; i < 9; i++) {
            int slotX = startX + i * (slotSize + spacing);
            
            // Draw slot background
            if (i == player.getSelectedSlot()) {
                g2d.setColor(new Color(255, 255, 0, 100)); // Highlighted
            } else {
                g2d.setColor(new Color(128, 128, 128, 100)); // Normal
            }
            g2d.fillRect(slotX, hotbarY, slotSize, slotSize);
            
            // Draw slot border
            if (i == player.getSelectedSlot()) {
                g2d.setColor(Color.YELLOW);
                g2d.setStroke(new BasicStroke(2));
            } else {
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(1));
            }
            g2d.drawRect(slotX, hotbarY, slotSize, slotSize);
            
            // Draw item in slot
            ItemStack item = hotbar[i];
            if (item != null && !item.isEmpty()) {
                // Draw item (using tile color for now)
                g2d.setColor(item.getItemType().getColor());
                g2d.fillRect(slotX + 8, hotbarY + 8, slotSize - 16, slotSize - 16);
                
                // Draw item border
                g2d.setColor(Color.BLACK);
                g2d.drawRect(slotX + 8, hotbarY + 8, slotSize - 16, slotSize - 16);
                
                // Draw quantity
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                String quantityStr = String.valueOf(item.getQuantity());
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(quantityStr);
                g2d.drawString(quantityStr, 
                              slotX + slotSize - textWidth - 5, 
                              hotbarY + slotSize - 5);
            }
            
            // Draw slot number
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            g2d.drawString(String.valueOf(i + 1), slotX + 2, hotbarY + 12);
        }
    }
    
    private void drawUI(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Inventory System Demo", 10, 20);
        g2d.drawString("Controls: A/D - Move, Space - Jump", 10, 35);
        g2d.drawString("Mouse: Left - Break Block, Right - Place Block", 10, 50);
        g2d.drawString("Keys 1-9: Select Hotbar Slot", 10, 65);
        g2d.drawString("Current Slot: " + (player.getSelectedSlot() + 1), 10, 80);
        
        ItemStack selected = player.getSelectedItem();
        if (selected != null) {
            g2d.drawString("Selected: " + selected.getItemType().name() + 
                          " (" + selected.getQuantity() + ")", 10, 95);
        } else {
            g2d.drawString("Selected: Empty", 10, 95);
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        int worldX = (e.getX() + camera.getX()) / World.TILE_SIZE;
        int worldY = (e.getY() + camera.getY()) / World.TILE_SIZE;
        
        if (e.getButton() == MouseEvent.BUTTON1) {
            // Left click - break block and add to inventory
            TileType tileToBreak = world.getTile(worldX, worldY);
            if (tileToBreak != TileType.AIR) {
                world.setTile(worldX, worldY, TileType.AIR);
                player.collectItem(tileToBreak, 1);
                System.out.println("Collected: " + tileToBreak.name());
            }
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            // Right click - place block from inventory
            ItemStack selectedItem = player.getSelectedItem();
            if (selectedItem != null && world.getTile(worldX, worldY) == TileType.AIR) {
                world.setTile(worldX, worldY, selectedItem.getItemType());
                player.useSelectedItem();
                System.out.println("Placed: " + selectedItem.getItemType().name());
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
        JFrame frame = new JFrame("Terraria Clone - Inventory System");
        InventoryGame game = new InventoryGame();
        
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