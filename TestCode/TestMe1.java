package TestCode;
import java.awt.*;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;


public class TestMe1 extends JFrame {
  
  private static final int WINDOW_WIDTH = 1200;
  private static final int WINDOW_HEIGHT = 800;

  public TestMe1(){

    setTitle("title");
    setSize(WINDOW_WIDTH,WINDOW_HEIGHT);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    setResizable(true);


    getContentPane().setBackground(Color.GREEN);

    setVisible(true);

    System.out.println("Game window created successfully!");
    System.out.println("Window size: " + WINDOW_WIDTH + "x" + WINDOW_HEIGHT);
  
}


public static void main (String[] args){

  SwingUtilities.invokeLater(() -> {
    new TestMe1();
  });

  }
}

