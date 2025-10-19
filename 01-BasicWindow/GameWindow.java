import java.awt.*;
import javax.swing.*;

/**
 * Lesson 1: Creating a Basic Game Window
 * 
 * This is your first step in Java game development!
 * We'll create a simple window using Java Swing.
 */
public class GameWindow extends JFrame {
    
    // Window dimensions
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    
    public GameWindow() {
        // Set up the window
        setTitle("My First Java Game");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
        setResizable(false); // Keep window size fixed for now
        
        // Set background color
        getContentPane().setBackground(Color.BLACK);
        
        // Make the window visible
        setVisible(true);
        
        System.out.println("Game window created successfully!");
        System.out.println("Window size: " + WINDOW_WIDTH + "x" + WINDOW_HEIGHT);
    }
    
    public static void main(String[] args) {
        // Create the game window on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new GameWindow();
        });
    }
}