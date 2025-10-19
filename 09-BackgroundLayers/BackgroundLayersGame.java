import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

/**
 * Lesson 9: Background Layers (Parallax)
 * Adds multi-layered backgrounds with parallax scrolling for depth.
 */
public class BackgroundLayersGame extends JPanel implements Runnable, KeyListener {
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final int TARGET_FPS = 60;
    private static final long TARGET_TIME = 1000000000 / TARGET_FPS;

    private boolean running = false;
    private Thread gameThread;
    private int cameraX = 0;
    private int playerX = 400;
    private int playerY = 300;
    private boolean leftPressed = false, rightPressed = false;

    // Parallax background layers
    private ParallaxLayer[] layers;

    public BackgroundLayersGame() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        // Create 3 background layers with different speeds/colors
        layers = new ParallaxLayer[] {
            new ParallaxLayer(new Color(30, 30, 60), 0.2f, 40, 120), // Farthest (mountains)
            new ParallaxLayer(new Color(60, 120, 180), 0.4f, 80, 200), // Middle (hills)
            new ParallaxLayer(new Color(120, 200, 255), 0.7f, 160, 320) // Closest (trees)
        };
    }

    public void startGame() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void stopGame() {
        running = false;
        try {
            if (gameThread != null) gameThread.join();
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
                try { Thread.sleep(waitTime / 1000000); } catch (InterruptedException e) { break; }
            }
        }
    }

    private void update() {
        if (leftPressed) playerX -= 4;
        if (rightPressed) playerX += 4;
        cameraX = playerX - WINDOW_WIDTH / 2;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        // Draw parallax background layers
        for (ParallaxLayer layer : layers) {
            layer.draw(g2d, cameraX, WINDOW_WIDTH, WINDOW_HEIGHT);
        }
        // Draw ground
        g2d.setColor(new Color(80, 60, 40));
        g2d.fillRect(0, WINDOW_HEIGHT - 80, WINDOW_WIDTH, 80);
        // Draw player
        g2d.setColor(Color.ORANGE);
        g2d.fillRect(playerX - cameraX - 16, playerY - 32, 32, 32);
        // UI
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Lesson 9: Parallax Backgrounds (A/D to move)", 20, 30);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_LEFT) leftPressed = true;
        if (e.getKeyCode() == KeyEvent.VK_D || e.getKeyCode() == KeyEvent.VK_RIGHT) rightPressed = true;
    }
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_LEFT) leftPressed = false;
        if (e.getKeyCode() == KeyEvent.VK_D || e.getKeyCode() == KeyEvent.VK_RIGHT) rightPressed = false;
    }
    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Terraria Clone - Parallax Backgrounds");
        BackgroundLayersGame game = new BackgroundLayersGame();
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

class ParallaxLayer {
    private Color color;
    private float speed;
    private int featureHeight;
    private int featureWidth;
    private Random rand = new Random();

    public ParallaxLayer(Color color, float speed, int featureHeight, int featureWidth) {
        this.color = color;
        this.speed = speed;
        this.featureHeight = featureHeight;
        this.featureWidth = featureWidth;
    }

    public void draw(Graphics2D g2d, int cameraX, int windowWidth, int windowHeight) {
        int baseY = windowHeight - featureHeight - 80;
        g2d.setColor(color);
        // Draw repeated features (mountains/hills/trees)
        for (int x = -featureWidth; x < windowWidth + featureWidth; x += featureWidth) {
            int worldX = (int) ((x + cameraX) * speed);
            int heightVar = featureHeight + rand.nextInt(20) - 10;
            g2d.fillRect(x - (int)(cameraX * speed), baseY - heightVar, featureWidth, heightVar);
        }
    }
}
