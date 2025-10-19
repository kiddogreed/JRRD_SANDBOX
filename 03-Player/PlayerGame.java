import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.*;

/**
 * Lesson 3: Creating a Player Character
 * 
 * This introduces:
 * 1. A Player class to represent the player character
 * 2. Sprite-like rendering (colored rectangles for now)
 * 3. Gravity and jumping mechanics
 * 4. Basic collision with ground
 */

class Player {
    private int x, y;
    private int width, height;
    private int velocityX, velocityY;
    private boolean onGround;
    private Color color;
    
    // Constants
    private static final int GRAVITY = 1;
    private static final int JUMP_STRENGTH = -15;
    private static final int MOVE_SPEED = 5;
    
    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.width = 30;
        this.height = 40;
        this.velocityX = 0;
        this.velocityY = 0;
        this.onGround = false;
        this.color = Color.BLUE;
    }
    
    public void update(int screenWidth, int screenHeight) {
        // Apply gravity
        if (!onGround) {
            velocityY += GRAVITY;
        }
        
        // Update position
        x += velocityX;
        y += velocityY;
        
        // Simple ground collision (bottom of screen)
        if (y + height >= screenHeight - 50) { // 50 pixels from bottom as "ground"
            y = screenHeight - 50 - height;
            velocityY = 0;
            onGround = true;
        } else {
            onGround = false;
        }
        
        // Keep player on screen horizontally
        if (x < 0) x = 0;
        if (x + width > screenWidth) x = screenWidth - width;
        
        // Reset horizontal velocity (no momentum)
        velocityX = 0;
    }
    
    public void moveLeft() {
        velocityX = -MOVE_SPEED;
        color = Color.CYAN; // Visual feedback for movement
    }
    
    public void moveRight() {
        velocityX = MOVE_SPEED;
        color = Color.MAGENTA; // Visual feedback for movement
    }
    
    public void jump() {
        if (onGround) {
            velocityY = JUMP_STRENGTH;
            onGround = false;
            color = Color.YELLOW; // Visual feedback for jumping
        }
    }
    
    public void idle() {
        color = Color.BLUE; // Default color when not moving
    }
    
    public void draw(Graphics2D g2d) {
        // Draw player body
        g2d.setColor(color);
        g2d.fillRect(x, y, width, height);
        
        // Draw player outline
        g2d.setColor(Color.WHITE);
        g2d.drawRect(x, y, width, height);
        
        // Draw simple "face"
        g2d.setColor(Color.WHITE);
        g2d.fillOval(x + 5, y + 5, 5, 5); // Left eye
        g2d.fillOval(x + 20, y + 5, 5, 5); // Right eye
        g2d.drawLine(x + 10, y + 20, x + 20, y + 20); // Mouth
    }
    
    // Getters for position (useful for camera following, etc.)
    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isOnGround() { return onGround; }
}

public class PlayerGame extends JPanel implements Runnable, KeyListener {
    
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final int TARGET_FPS = 60;
    private static final long TARGET_TIME = 1000000000 / TARGET_FPS;
    
    private boolean running = false;
    private Thread gameThread;
    private Player player;
    
    // Key states
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean spacePressed = false;
    
    public PlayerGame() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        
        // Create player at center of screen
        player = new Player(WINDOW_WIDTH / 2, 100);
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
            
            // Frame rate control
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
        // Handle player input
        if (leftPressed) {
            player.moveLeft();
        } else if (rightPressed) {
            player.moveRight();
        } else {
            player.idle();
        }
        
        if (spacePressed) {
            player.jump();
        }
        
        // Update player
        player.update(WINDOW_WIDTH, WINDOW_HEIGHT);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                           RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw ground
        g2d.setColor(Color.GREEN);
        g2d.fillRect(0, WINDOW_HEIGHT - 50, WINDOW_WIDTH, 50);
        
        // Draw player
        player.draw(g2d);
        
        // Draw UI
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Controls:", 10, 30);
        g2d.drawString("A/D or Arrow Keys: Move", 10, 50);
        g2d.drawString("SPACE: Jump", 10, 70);
        g2d.drawString("Player Position: (" + player.getX() + ", " + player.getY() + ")", 10, 100);
        g2d.drawString("On Ground: " + player.isOnGround(), 10, 120);
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                leftPressed = true;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                rightPressed = true;
                break;
            case KeyEvent.VK_SPACE:
                spacePressed = true;
                break;
            default:
                break;
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                leftPressed = false;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                rightPressed = false;
                break;
            case KeyEvent.VK_SPACE:
                spacePressed = false;
                break;
            default:
                break;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Player Character Example");
        PlayerGame game = new PlayerGame();
        
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