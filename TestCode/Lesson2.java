package TestCode;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.*;

public class Lesson2 extends JPanel implements Runnable, KeyListener {

  private static final int WINDOW_WIDTH = 1200;
  private static final int WINDOW_HEIGHT = 800;
  private static final int TARGET_FPS = 60;
  private static final long TARGET_TIME = 1000000000 / TARGET_FPS;

  // Game state
  private boolean running = false;
  private Thread gameThread;

  //simple animated rectangle to show the loop working
  private int rectX = 200;
  private int rectY = 200;  
  private int rectVelX = 5;
  private int rectVelY = 5;
  private Color rectColor = Color.MAGENTA;

  // key states
  private boolean leftPressed = false;
  private boolean rightPressed = false;
  private boolean upPressed = false;
  private boolean downPressed = false;

  public Lesson2(){
    setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
    setBackground(Color.BLACK);
    setFocusable(true);
    addKeyListener(this);
  }
  
  public void startGame(){
    running = true;
    gameThread = new Thread(this);
    gameThread.start();
  }

  public void stopGame(){
    running = false;
    try{
      gameThread.join();
    } catch (InterruptedException e){
      e.printStackTrace();
    }
  }

  @Override
  public void run(){
    long lastTime = System.nanoTime();

    while(running){
      long currentTime = System.nanoTime();

      // update game state
      update();

      // render everything
      repaint();

      long elapsed = System.nanoTime() - currentTime;
      long waitTime = TARGET_TIME - elapsed;

      if (waitTime > 0){
        try{
          Thread.sleep(waitTime / 1000000);
        } catch (InterruptedException e){
          break;
        }
      }
    }
}

private void update(){
  // update rectangle position based  on key input

  if(leftPressed&& rectX >0 ){
    rectX -=3;
  } 
  if(rightPressed && rectX < WINDOW_WIDTH -50){
    rectX +=3;
  }
  if(upPressed && rectY >0){
    rectY -=3;
  }
  if(downPressed && rectY < WINDOW_HEIGHT -50){
    rectY +=3;
  }

  // auto-bouncing animation when no keys are pressed
  if(!leftPressed && !rightPressed && !upPressed && !downPressed){
    rectX += rectVelX;
    rectY += rectVelY;

    //bounce off walls
    if(rectX <= 0 || rectX >= WINDOW_WIDTH -50){
      rectVelX = -rectVelX;
      rectColor = new Color((int)(Math.random() * 255), 
                            (int)(Math.random() * 255), 
                            (int)(Math.random() * 255));
    }
    if (rectY <= 0 || rectY >= WINDOW_HEIGHT - 50) {
                rectVelY = -rectVelY;
                rectColor = new Color((int)(Math.random() * 255), 
                                    (int)(Math.random() * 255), 
                                    (int)(Math.random() * 255));
            }
  }
}

@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Cast to Graphics2D for better drawing capabilities
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable anti-aliasing for smoother graphics
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                           RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw the animated rectangle
        g2d.setColor(rectColor);
        g2d.fillRect(rectX, rectY, 50, 50);
        
        // Draw a border
        g2d.setColor(Color.WHITE);
        g2d.drawRect(rectX, rectY, 50, 50);
        
        // Draw instructions
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Use ARROW KEYS to control the square", 10, 30);
        g2d.drawString("Release keys to see auto-bouncing animation", 10, 50);
        g2d.drawString("Position: (" + rectX + ", " + rectY + ")", 10, WINDOW_HEIGHT - 20);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                leftPressed = true;
                break;
            case KeyEvent.VK_RIGHT:
                rightPressed = true;
                break;
            case KeyEvent.VK_UP:
                upPressed = true;
                break;
            case KeyEvent.VK_DOWN:
                downPressed = true;
                break;
        }
    }


     @Override
     public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                leftPressed = false;
                break;
            case KeyEvent.VK_RIGHT:
                rightPressed = false;
                break;
            case KeyEvent.VK_UP:
                upPressed = false;
                break;
            case KeyEvent.VK_DOWN:
                downPressed = false;
                break;
        }
    }
    
    public void keyTyped(KeyEvent e) {
        // Not used, but required by KeyListener interface
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Game Loop Example");
        Lesson2 game = new Lesson2();
        
        frame.add(game);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        // Start the game loop
        game.startGame();
        
        // Add a shutdown hook to properly stop the game
        Runtime.getRuntime().addShutdownHook(new Thread(game::stopGame));
    }
}