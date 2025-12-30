import java.awt.*;

public class Player {
    int x, y, width, height;
    Color color = Color.CYAN;

    public Player(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }

    public void move(int dx, int panelWidth) {
        x += dx;
        if (x < 0) x = 0;
        if (x + width > panelWidth) x = panelWidth - width;
    }

    public void draw(Graphics2D g2) {
        g2.setColor(color);
        g2.fillRect(x, y, width, height);
        g2.fillRect(x + width / 2 - 5, y - 10, 10, 10);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}