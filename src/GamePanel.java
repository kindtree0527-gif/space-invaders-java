import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GamePanel extends JPanel implements ActionListener, KeyListener {

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    private Player player;
    private List<Enemy> enemies = new ArrayList<>();
    private List<Bullet> bullets = new ArrayList<>();       // 自機の弾
    private List<Bullet> enemyBullets = new ArrayList<>();  // 敵の弾
    private List<Barrier> barriers = new ArrayList<>();     // 盾

    private int enemyDx = 2;
    private int enemyStepDown = 10;

    private Timer timer;

    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean spacePressed = false;

    private boolean gameOver = false;
    private boolean gameClear = false;

    // START画面
    private boolean showStartScreen = true;
    private JButton startButton;

    // 自機の連射制限
    private long lastShotTime = 0;
    private long shotInterval = 200;

    // 敵の弾
    private long lastEnemyShotTime = 0;
    private long enemyShotInterval = 800;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        setLayout(null);
        addKeyListener(this);

        // STARTボタン
        startButton = new JButton("START");
        startButton.setBounds(WIDTH / 2 - 75, HEIGHT / 2 - 25, 150, 50);
        startButton.setFont(new Font("Arial", Font.BOLD, 20));
        startButton.addActionListener(e -> {
            showStartScreen = false;
            remove(startButton);
            initGame();
            requestFocusInWindow();
        });
        add(startButton);

        timer = new Timer(16, this);
        timer.start();
    }

    private void initGame() {
        player = new Player(WIDTH / 2 - 20, HEIGHT - 80, 40, 20);

        // 敵配置
        enemies.clear();
        int rows = 4;
        int cols = 10;
        int startX = 100;
        int startY = 60;
        int hGap = 50;
        int vGap = 40;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                enemies.add(new Enemy(startX + c * hGap, startY + r * vGap, 30, 20));
            }
        }

        bullets.clear();
        enemyBullets.clear();

        // 盾配置
        barriers.clear();
        int baseY = HEIGHT - 180;
        int spacing = 150;
        int barrierStartX = 100;
        for (int i = 0; i < 4; i++) {
            barriers.add(new Barrier(barrierStartX + i * spacing, baseY));
        }

        enemyDx = 2;
        gameOver = false;
        gameClear = false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!showStartScreen && !gameOver && !gameClear) {
            updateGame();
        }
        repaint();
    }

    private void updateGame() {

        // 自機移動
        if (leftPressed && !rightPressed) player.move(-5, WIDTH);
        if (rightPressed && !leftPressed) player.move(5, WIDTH);

        // 自機弾発射
        if (spacePressed) shootBullet();

        // 敵の移動
        boolean needReverse = false;
        for (Enemy enemy : enemies) {
            enemy.x += enemyDx;
            if (enemy.x < 20 || enemy.x + enemy.width > WIDTH - 20) {
                needReverse = true;
            }
        }

        if (needReverse) {
            enemyDx = -enemyDx;
            for (Enemy enemy : enemies) {
                enemy.y += enemyStepDown;
                if (enemy.y + enemy.height >= HEIGHT - 100) {
                    gameOver = true;
                }
            }
        }

        // 敵の弾発射
        enemyShoot();

        // 自機弾の移動 & 盾との判定 & 敵との判定
        Iterator<Bullet> bulletIt = bullets.iterator();
        while (bulletIt.hasNext()) {
            Bullet b = bulletIt.next();
            b.y -= b.speed;

            // 盾との衝突
            boolean hitBarrier = false;
            for (Barrier barrier : barriers) {
                if (barrier.hit(b.x, b.y)) {
                    hitBarrier = true;
                    break;
                }
            }
            if (hitBarrier) {
                bulletIt.remove();
                continue;
            }

            // 画面外
            if (b.y < 0) {
                bulletIt.remove();
                continue;
            }

            // 敵との衝突判定
            boolean hitEnemy = false;
            Iterator<Enemy> enemyIt = enemies.iterator();
            while (enemyIt.hasNext()) {
                Enemy enemy = enemyIt.next();
                if (b.getBounds().intersects(enemy.getBounds())) {
                    enemyIt.remove();
                    hitEnemy = true;
                    break;
                }
            }
            if (hitEnemy) {
                bulletIt.remove();
            }
        }

        // 敵弾の移動 & 盾との判定 & 自機との判定
        Iterator<Bullet> ebIt = enemyBullets.iterator();
        while (ebIt.hasNext()) {
            Bullet b = ebIt.next();
            // enemy bullet: speed が負なので下に進む
            b.y -= b.speed;

            // 盾との衝突
            boolean hitBarrier = false;
            for (Barrier barrier : barriers) {
                if (barrier.hit(b.x, b.y)) {
                    hitBarrier = true;
                    break;
                }
            }
            if (hitBarrier) {
                ebIt.remove();
                continue;
            }

            // 自機との衝突
            if (player.getBounds().intersects(b.getBounds())) {
                gameOver = true;
                ebIt.remove();
                continue;
            }

            // 画面外
            if (b.y > HEIGHT) {
                ebIt.remove();
            }
        }

        // 敵 vs 自機
        for (Enemy enemy : enemies) {
            if (enemy.getBounds().intersects(player.getBounds())) {
                gameOver = true;
                break;
            }
        }

        // 全部倒したらクリア
        if (enemies.isEmpty()) {
            gameClear = true;
        }
    }

    private void shootBullet() {
        long now = System.currentTimeMillis();
        if (now - lastShotTime < shotInterval) return;
        lastShotTime = now;

        bullets.add(new Bullet(
                player.x + player.width / 2 - 2,
                player.y,
                4, 10, 8
        ));
    }

    private void enemyShoot() {
        if (enemies.isEmpty()) return;

        long now = System.currentTimeMillis();
        if (now - lastEnemyShotTime < enemyShotInterval) return;
        lastEnemyShotTime = now;

        Enemy shooter = enemies.get((int) (Math.random() * enemies.size()));

        enemyBullets.add(new Bullet(
                shooter.x + shooter.width / 2 - 2,
                shooter.y + shooter.height,
                4, 10, -6   // マイナスで下方向
        ));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (showStartScreen) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Monospaced", Font.BOLD, 32));
            String title = "SPACE INVADERS";
            int tw = g2.getFontMetrics().stringWidth(title);
            g2.drawString(title, (WIDTH - tw) / 2, HEIGHT / 2 - 60);
            return;
        }

        // 盾
        for (Barrier barrier : barriers) {
            barrier.draw(g2);
        }

        // 自機
        if (!gameOver) {
            player.draw(g2);
        }

        // 敵
        for (Enemy enemy : enemies) {
            enemy.draw(g2);
        }

        // 自機弾（黄色）
        g2.setColor(Color.YELLOW);
        for (Bullet b : bullets) {
            g2.fillRect(b.x, b.y, b.width, b.height);
        }

        // 敵弾（赤）
        g2.setColor(Color.RED);
        for (Bullet b : enemyBullets) {
            g2.fillRect(b.x, b.y, b.width, b.height);
        }

        // 残り敵数
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g2.drawString("Enemies: " + enemies.size(), 20, 20);

        if (gameOver) drawCenter(g2, "GAME OVER - Press R to Restart");
        if (gameClear) drawCenter(g2, "YOU WIN! - Press R to Restart");
    }

    private void drawCenter(Graphics2D g2, String text) {
        FontMetrics fm = g2.getFontMetrics();
        int x = (WIDTH - fm.stringWidth(text)) / 2;
        int y = HEIGHT / 2;
        g2.drawString(text, x, y);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (showStartScreen) return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> leftPressed = true;
            case KeyEvent.VK_RIGHT -> rightPressed = true;
            case KeyEvent.VK_SPACE -> spacePressed = true;
            case KeyEvent.VK_R -> {
                if (gameOver || gameClear) initGame();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (showStartScreen) return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> leftPressed = false;
            case KeyEvent.VK_RIGHT -> rightPressed = false;
            case KeyEvent.VK_SPACE -> spacePressed = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}