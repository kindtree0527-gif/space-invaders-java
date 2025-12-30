import java.awt.*;

public class Barrier {

    int x, y;
    int cellSize = 6;

    int cols = 12;   // 横セル数
    int rows = 8;    // 縦セル数

    int[][] hp;      // 耐久値（0 = 破壊）

    public Barrier(int x, int y) {
        this.x = x;
        this.y = y;

        hp = new int[rows][cols];

        // アーチ型シールドの形
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                boolean inside =
                        (r >= 3) ||
                                (r == 2 && c >= 2 && c <= 9) ||
                                (r == 1 && c >= 3 && c <= 8) ||
                                (r == 0 && c >= 4 && c <= 7);

                if (inside) {
                    hp[r][c] = 3;  // 耐久値3
                } else {
                    hp[r][c] = 0;
                }
            }
        }
    }

    public void draw(Graphics2D g2) {

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                if (hp[r][c] <= 0) continue;

                int px = x + c * cellSize;
                int py = y + r * cellSize;

                Color color = switch (hp[r][c]) {
                    case 3 -> new Color(0, 200, 0);   // 緑
                    case 2 -> new Color(150, 200, 0); // 黄緑
                    case 1 -> new Color(200, 150, 0); // オレンジ
                    default -> Color.RED;
                };

                g2.setColor(color);
                g2.fillRect(px, py, cellSize, cellSize);
            }
        }
    }

    // 弾が当たったら該当セルの耐久値を減らす
    public boolean hit(int bx, int by) {
        int cx = (bx - x) / cellSize;
        int cy = (by - y) / cellSize;

        if (cx < 0 || cx >= cols || cy < 0 || cy >= rows) return false;

        if (hp[cy][cx] > 0) {
            hp[cy][cx]--;
            return true;
        }
        return false;
    }

    public boolean isDestroyed() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (hp[r][c] > 0) return false;
            }
        }
        return true;
    }
}