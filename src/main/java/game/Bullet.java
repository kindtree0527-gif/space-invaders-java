import java.awt.*;

public class Bullet {
    int x, y, width, height;
    int speed;

    public Bullet(int x, int y, int w, int h, int speed) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
        this.speed = speed;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}