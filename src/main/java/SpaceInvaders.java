import javax.swing.*;

public class SpaceInvaders extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SpaceInvaders::new);
    }

    public SpaceInvaders() {
        setTitle("Space Invaders - Simple Java Version");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        GamePanel panel = new GamePanel();
        add(panel);
        pack();

        setLocationRelativeTo(null);
        setVisible(true);
    }
}