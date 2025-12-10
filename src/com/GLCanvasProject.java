package com;

import com.sun.opengl.util.j2d.TextRenderer;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import java.awt.*;
import java.util.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

/**
 * GLCanvasProject - لعبة بسيطة (Ball / Paddle / Bricks) مع مؤقت و حياة وطبقات مستويات.
 * تم تنظيف الكود وإضافة تعليقات عربية لسهولة القراءة.
 */
public class GLCanvasProject implements GLEventListener, KeyListener,
        java.awt.event.MouseListener, java.awt.event.MouseMotionListener {

    // ----- Viewport -----
    private double left = -225, right = 225, bottom = -150, top = 150;

    // ----- Game objects -----
    private Paddle paddleLeft, paddleRight;
    private Ball ball;
    private boolean started = false;
    private List<Brick> bricks = new ArrayList<>();
    private int score = 0;

    private int level = 1;
    private double currentBallSpeed = 3.0;

    private int patternIndex = 1;
    private Random rand = new Random();

    // ----- Input states -----
    private boolean leftArrow, rightArrow, upArrow, downArrow;
    private boolean aKey, dKey, wKey, sKey;

    private TextRenderer textRenderer;
    private SoundManager soundManager; // افتراضي: افترض أن لديك SoundManager في المشروع

    private final double PADDLE_SPEED = 3.0;
    private final double BALL_SPEED = 3.0;

    // mode: 1 => single player, 2 => two players (حفظ فقط للمنطق، نتحكم عبر setPlayers)
    private int players = 1;

    private boolean levelCompleted = false;
    private boolean gameOver = false;

    // flags for paddles (active = visible & participating)
    private boolean leftActive = false;
    private boolean rightActive = true; // الافتراضي: لاعب واحد على اليمين

    // lives
    private int lives = 3;
    private final int maxLives = 3;

    // ===== TIMER VARIABLES - متغييرات المؤقت =====
    private long levelStartTimeMillis = 0;           // وقت بداية المستوى بالمللي ثانية
    private final long TIME_LIMIT_SECONDS = 30;      // الحد الأقصى للوقت بالمستوى (ثواني)
    private long timeElapsedSeconds = 0;             // الوقت المنقضي الآن
    private boolean timerActive = false;             // هل المؤقت يعمل؟

    public GLCanvasProject() {
        // الافتراضي: players = 1، يمكن استدعاء setPlayers قبل init
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL gl = glAutoDrawable.getGL();
        gl.glClearColor(0.65f, 0.55f, 0.45f, 1.0f);

        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(left, right, bottom, top, -1, 1);

        double paddleW = 90, paddleH = 12;
        double initialY = bottom + 40;

        // Position paddles
        paddleRight = new Paddle(20, initialY, paddleW, paddleH);
        if (leftActive) {
            paddleLeft = new Paddle(-110, initialY, paddleW, paddleH);
        } else {
            // وضع اليسار بعيدًا خارج الشاشة لتجنب التصادم / العرض إذا لم يكن مفعلًا
            paddleLeft = new Paddle(left - 500, initialY, paddleW, paddleH);
        }

        resetBallAttached();
        choosePatternForLevel();
        createBricksByLevel();

        textRenderer = new TextRenderer(new Font("Arial", Font.BOLD, 16));
        soundManager = new SoundManager(); // افترض وجود مُنفذ

        lives = maxLives;
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        GL gl = glAutoDrawable.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);

        // تحديث حركة المقابض حسب الإدخال
        updatePaddles();

        // إذا لم يبدأ اللعب، اعلّق الكرة فوق المضرب المناسب
        if (!started) {
            attachBallAboveActivePaddle();
        } else {
            updateBall();
        }

        // لون الطوب بحسب المستوى
        Color levelColor = getColorForLevel(level);

        // رسم الطوب
        for (Brick b : bricks) {
            drawBrick(gl, b.x, b.y, b.w, b.h, levelColor);
        }

        // رسم المقابض فقط إذا كانت مفعلة
        if (leftActive) {
            drawFancyPaddle(gl, paddleLeft.x, paddleLeft.y, paddleLeft.x + paddleLeft.w, paddleLeft.y + paddleLeft.h);
        }
        if (rightActive) {
            drawFancyPaddle(gl, paddleRight.x, paddleRight.y, paddleRight.x + paddleRight.w, paddleRight.y + paddleRight.h);
        }

        // رسم الكرة
        drawCircle(gl, ball.x + ball.size / 2, ball.y + ball.size / 2, ball.size / 2, 1f, 0.9f, 0.0f);

        // رسم النصوص (score, level, mode, timer)
        textRenderer.beginRendering(500, 300);
        textRenderer.setColor(1.0f, 1.0f, 0f, 1.0f);
        textRenderer.draw("Score: " + score, 10, 10);
        textRenderer.draw("Level: " + level, 400, 10);
        String modeLabel = (leftActive && rightActive) ? "Two Players" : (rightActive ? "One Player" : "One Player (Left?)");
        textRenderer.draw("Mode: " + modeLabel, 200, 10);

        // ===== TIMER DISPLAY - عرض المؤقت =====
        if (!gameOver) {
            long timeLeft = TIME_LIMIT_SECONDS - timeElapsedSeconds;
            if (started && timerActive) {
                // احمر اذا تبقى 10 ثواني او اقل
                textRenderer.setColor(timeLeft <= 10 ? 0.9f : 1.0f,
                        timeLeft <= 10 ? 0.0f : 1.0f,
                        0f, 1.0f);
                textRenderer.draw("Time Left: " + Math.max(0, timeLeft), 200, 270);
            } else if (!started && TIME_LIMIT_SECONDS > 0) {
                textRenderer.setColor(1.0f, 1.0f, 0f, 1.0f);
                textRenderer.draw("Press ENTER to Start. Time Limit: " + TIME_LIMIT_SECONDS + "s", 100, 270);
            }
        }
        textRenderer.endRendering();
        // ===== END TIMER DISPLAY =====

        // رسم القلوب (الحياة)
        drawLives(gl);
    }

    @Override public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {}
    @Override public void displayChanged(GLAutoDrawable d, boolean b, boolean c) {}

    // ----- رسم المربعات والمقابض والكرة -----
    private void drawBrick(GL gl, double x, double y, double w, double h, Color fillColor) {
        gl.glColor3f(fillColor.getRed()/255f, fillColor.getGreen()/255f, fillColor.getBlue()/255f);
        gl.glBegin(GL.GL_POLYGON);
        gl.glVertex2d(x, y);
        gl.glVertex2d(x + w, y);
        gl.glVertex2d(x + w, y + h);
        gl.glVertex2d(x, y + h);
        gl.glEnd();

        gl.glLineWidth(3.5f);
        gl.glColor3f(0f, 0f, 0f);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2d(x, y);
        gl.glVertex2d(x + w, y);
        gl.glVertex2d(x + w, y + h);
        gl.glVertex2d(x, y + h);
        gl.glEnd();
    }

    private void drawFancyPaddle(GL gl, double x1, double y1, double x2, double y2) {
        double shadowOffset = -4;
        gl.glColor4f(0f, 0f, 0f, 0.35f);
        gl.glBegin(GL.GL_POLYGON);
        gl.glVertex2d(x1 + 3, y1 + shadowOffset);
        gl.glVertex2d(x2 + 3, y1 + shadowOffset);
        gl.glVertex2d(x2 + 3, y2 + shadowOffset);
        gl.glVertex2d(x1 + 3, y2 + shadowOffset);
        gl.glEnd();

        gl.glBegin(GL.GL_POLYGON);
        gl.glColor3f(0.0f, 0.3f, 0.8f);
        gl.glVertex2d(x1, y2);
        gl.glVertex2d(x2, y2);

        gl.glColor3f(0.3f, 0.6f, 1.0f);
        gl.glVertex2d(x2, y1);
        gl.glVertex2d(x1, y1);
        gl.glEnd();

        gl.glLineWidth(4f);
        gl.glColor3f(0f, 0f, 0f);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2d(x1, y2);
        gl.glVertex2d(x2, y2);
        gl.glVertex2d(x2, y1);
        gl.glVertex2d(x1, y1);
        gl.glEnd();
    }

    private void drawCircle(GL gl, double cx, double cy, double radius, float r, float g, float b) {
        gl.glColor3f(r, g, b);
        gl.glBegin(GL.GL_POLYGON);
        int steps = 32;
        for (int i = 0; i < steps; i++) {
            double ang = 2 * Math.PI * i / steps;
            gl.glVertex2d(Math.cos(ang) * radius + cx, Math.sin(ang) * radius + cy);
        }
        gl.glEnd();
    }

    // ----- Hearts (lives) -----
    private void drawLives(GL gl) {
        double size = 9; // حجم القلب
        double margin = 12;
        for (int i = 0; i < maxLives; i++) {
            double xCenter = right - margin - (i * (size + 6)) - size / 2.0;
            double yCenter = top - margin - size / 2.0;
            boolean filled = (i < lives);
            drawHeart(gl, xCenter, yCenter, size, filled);
        }
    }

    private void drawHeart(GL gl, double cx, double cy, double size, boolean filled) {
        int segments = 100;
        double scale = size / 20.0;
        if (filled) {
            gl.glColor3f(0.85f, 0.05f, 0.15f);
            gl.glBegin(GL.GL_POLYGON);
        } else {
            gl.glColor3f(0.6f, 0.6f, 0.6f);
            gl.glLineWidth(2f);
            gl.glBegin(GL.GL_LINE_LOOP);
        }

        for (int i = 0; i < segments; i++) {
            double t = (2 * Math.PI * i) / segments;
            double x = 16 * Math.pow(Math.sin(t), 3);
            double y = 13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t);
            double px = x * scale + cx;
            double py = y * scale + cy;
            gl.glVertex2d(px, py);
        }
        gl.glEnd();
    }

    // ----- Game logic: إنشاء الطوب بحسب النمط والمستوى -----
    private void createBricksByLevel() {
        bricks.clear();

        int cols = 12;
        double brickW = 30;
        double brickH = 18;

        double totalWidth = right - left;
        double spacingX = (totalWidth - (cols * brickW)) / (cols + 1);

        int p = patternIndex;

        switch (p) {
            case 1: {
                int rows = level + 1;
                double startY = 40;
                for (int r = 0; r < rows; r++) {
                    double y = startY + r * (brickH + 5);
                    for (int c = 0; c < cols; c++) {
                        double x = left + spacingX + c * (brickW + spacingX);
                        bricks.add(new Brick(x, y, brickW, brickH));
                    }
                }
            } break;

            case 2: {
                int rows = Math.min(level + 2, 8);
                double centerX = (left + right) / 2.0;
                double startY = 60;
                for (int r = 0; r < rows; r++) {
                    int bricksInRow = cols - r * 2;
                    if (bricksInRow <= 0) break;
                    double rowWidth = bricksInRow * brickW + (bricksInRow - 1) * 6;
                    double x0 = centerX - rowWidth / 2.0;
                    double y = startY + r * (brickH + 6);
                    for (int c = 0; c < bricksInRow; c++) {
                        double x = x0 + c * (brickW + 6);
                        bricks.add(new Brick(x, y, brickW, brickH));
                    }
                }
            } break;

            case 3: {
                int rows = level + 2;
                double startY = 50;
                double centerX = (left + right) / 2.0;
                for (int r = 0; r < rows; r++) {
                    double y = startY + r * (brickH + 5);
                    int span = r;
                    for (int i = -span; i <= span; i++) {
                        double xLeft = centerX + i * (brickW + 4) - (brickW / 2.0) - (span * (brickW + 4)) / 2.0;
                        bricks.add(new Brick(xLeft, y, brickW, brickH));
                    }
                }
            } break;

            case 4: {
                int rows = level + 1;
                double startY = 45;
                for (int c = 0; c < cols; c++) {
                    double x = left + spacingX + c * (brickW + spacingX);
                    for (int r = 0; r < rows; r++) {
                        if (rand.nextDouble() < 0.18) continue;
                        double y = startY + r * (brickH + 6);
                        bricks.add(new Brick(x, y, brickW, brickH));
                    }
                }
            } break;

            case 5: {
                double centerX = (left + right) / 2.0;
                double[] relX = {-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5};
                int baseRows = level + 1;
                double startY = 60;
                for (int r = 0; r < baseRows; r++) {
                    double y = startY + r * (brickH + 5);
                    for (int i = 0; i < relX.length; i++) {
                        if (Math.abs(relX[i]) + r > 6) continue;
                        double x = centerX + relX[i] * (brickW + 2) - brickW / 2.0;
                        bricks.add(new Brick(x, y, brickW, brickH));
                    }
                }
            } break;

            case 6:
            default: {
                int rows = level + 2;
                double startY = 40;
                for (int r = 0; r < rows; r++) {
                    double y = startY + r * (brickH + 4);
                    for (int c = 0; c < cols; c++) {
                        if (rand.nextDouble() < 0.22 + (r * 0.02)) continue;
                        double x = left + spacingX + c * (brickW + spacingX);
                        bricks.add(new Brick(x, y, brickW, brickH));
                    }
                }
            } break;
        }
    }

    private void choosePatternForLevel() {
        patternIndex = ((level - 1) % 6) + 1;
        if (rand.nextDouble() < 0.18) {
            patternIndex = 1 + rand.nextInt(6);
        }
    }

    private Color getColorForLevel(int lvl) {
        switch (lvl % 5) {
            case 1: return new Color(139, 69, 19);
            case 2: return new Color(50, 205, 50);
            case 3: return new Color(65, 105, 225);
            case 4: return new Color(220, 20, 60);
            default: return new Color(128, 0, 128);
        }
    }

    // ----- تحديث المقابض بناءً على الإدخال -----
    private void updatePaddles() {
        if (rightActive) {
            if (leftArrow) paddleRight.moveX(-PADDLE_SPEED);
            if (rightArrow) paddleRight.moveX(PADDLE_SPEED);
            if (upArrow) paddleRight.moveY(PADDLE_SPEED);
            if (downArrow) paddleRight.moveY(-PADDLE_SPEED);
        }

        if (leftActive) {
            if (aKey) paddleLeft.moveX(-PADDLE_SPEED);
            if (dKey) paddleLeft.moveX(PADDLE_SPEED);
            if (wKey) paddleLeft.moveY(PADDLE_SPEED);
            if (sKey) paddleLeft.moveY(-PADDLE_SPEED);
        }

        if (leftActive) clampPaddle(paddleLeft);
        if (rightActive) clampPaddle(paddleRight);
    }

    private void clampPaddle(Paddle p) {
        if (p.x < left) p.x = left;
        if (p.x + p.w > right) p.x = right - p.w;
        double maxPaddleY = bottom + (top - bottom) * 0.25;
        if (p.y + p.h > maxPaddleY) p.y = maxPaddleY - p.h;
        if (p.y < bottom + 10) p.y = bottom + 10;
    }

    // ----- تحديث الكرة -----
    private void updateBall() {
        ball.x += ball.vx;
        ball.y += ball.vy;

        if (ball.x < left) {
            ball.x = left;
            ball.vx = Math.abs(ball.vx);
            if (soundManager != null) soundManager.playBallBounce();
        }
        if (ball.x + ball.size > right) {
            ball.x = right - ball.size;
            ball.vx = -Math.abs(ball.vx);
            if (soundManager != null) soundManager.playBallBounce();
        }
        if (ball.y + ball.size > top) {
            ball.y = top - ball.size;
            ball.vy = -Math.abs(ball.vy);
            if (soundManager != null) soundManager.playBallBounce();
        }

        // تصادم مع المقابض (فقط إذا كانت مفعّلة)
        if (leftActive) handlePaddleCollision(paddleLeft);
        if (rightActive) handlePaddleCollision(paddleRight);

        if (levelCompleted) return;

        // فحص تصادم مع الطوب
        Iterator<Brick> it = bricks.iterator();
        while (it.hasNext()) {
            Brick b = it.next();
            if (ball.intersects(b)) {
                it.remove();
                score++;
                if (soundManager != null) soundManager.playBrickExplode();
                ball.vy = -ball.vy;
                break;
            }
        }

        checkWin();
        checkLost();

        // استدعاء المؤقت
        updateTimerAndCheckTimeOut();
    }

    private void handlePaddleCollision(Paddle p) {
        if (ball.intersects(p)) {
            double hit = (ball.centerX() - (p.x + p.w / 2)) / (p.w / 2);
            ball.vx = hit * currentBallSpeed;
            if (Math.abs(ball.vx) < 1.5) ball.vx = 1.5 * (ball.vx > 0 ? 1 : -1);

            if (soundManager != null) soundManager.playBallBounce();
            ball.vy = Math.abs(ball.vy) + 0.2;
            ball.y = p.y + p.h;
        }
    }

    // ===== TIMER LOGIC - منطق المؤقت =====
    private void updateTimerAndCheckTimeOut() {
        if (started && !gameOver && timerActive) {
            long currentTimeMillis = System.currentTimeMillis();
            timeElapsedSeconds = (currentTimeMillis - levelStartTimeMillis) / 1000;

            if (timeElapsedSeconds >= TIME_LIMIT_SECONDS) {
                // انتهى الوقت -> خسارة فورية
                timerActive = false;
                gameOver = true;
                if (soundManager != null) soundManager.playGameLost();

                int choice = javax.swing.JOptionPane.showOptionDialog(null,
                        "Time's Up! You Lost!\nYour Score: " + score + "\nTime Limit: " + TIME_LIMIT_SECONDS + " seconds.",
                        "Game Over (Time Limit)",
                        javax.swing.JOptionPane.YES_NO_OPTION,
                        javax.swing.JOptionPane.INFORMATION_MESSAGE,
                        null,
                        new String[]{"Retry Level", "Back to Menu"},
                        "Retry Level");

                if (choice == 0) {
                    restartLevel();
                } else {
                    returnToMenu();
                }
            }
        } else if (!started && timerActive) {

            timerActive = false;
        }
    }


    private void checkWin() {
        if (bricks.isEmpty() && !levelCompleted) {
            levelCompleted = true;
            if (soundManager != null) soundManager.playLevelWin();


            timerActive = false;
            levelStartTimeMillis = 0;

            javax.swing.JOptionPane.showMessageDialog(null, "Level " + level + " Completed!");

            level++;
            currentBallSpeed += 0.5;
            started = false;

            choosePatternForLevel();
            createBricksByLevel();
            resetBallAttached();

            levelCompleted = false;
        }
    }

    private void checkLost() {
        if (ball.y + ball.size < bottom && !gameOver) {
            // الكرة سقطت للأسفل
            if (lives > 1) {
                lives--;
                if (soundManager != null) soundManager.playGameLost();
                javax.swing.JOptionPane.showMessageDialog(null, "You lost a life! Lives left: " + lives);

                // إعادة الكرة متعلقة دون إعادة الطوب أو المستوى
                resetBallAttached();
                gameOver = false;
                started = false;

                // إيقاف وإعادة تعيين المؤقت
                timerActive = false;
                levelStartTimeMillis = 0;
                timeElapsedSeconds = 0;
            } else {
                // آخر حياة -> Game Over
                lives = 0;
                gameOver = true;
                if (soundManager != null) soundManager.playGameLost();
                int choice = javax.swing.JOptionPane.showOptionDialog(null,
                        "You Lost!\nYour Score: " + score,
                        "Game Over",
                        javax.swing.JOptionPane.YES_NO_OPTION,
                        javax.swing.JOptionPane.INFORMATION_MESSAGE,
                        null,
                        new String[]{"Retry Level", "Back to Menu"},
                        "Retry Level");

                if (choice == 0) {
                    restartLevel();
                } else {
                    returnToMenu();
                }
            }
        }
    }

    private void restartLevel() {
        started = false;
        gameOver = false;
        resetBallAttached();
        createBricksByLevel();
        score = 0;
        lives = maxLives;

        // إعادة تعيين المؤقت
        timerActive = false;
        levelStartTimeMillis = 0;
        timeElapsedSeconds = 0;
    }

    private void returnToMenu() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            resetAll();
            started = false;
            java.awt.Window[] windows = java.awt.Window.getWindows();
            for (java.awt.Window w : windows) {
                if (w instanceof javax.swing.JFrame) {
                    javax.swing.JFrame frame = (javax.swing.JFrame) w;
                    frame.getContentPane().removeAll();
                    new GameWindow(); // افترض وجود GameWindow
                    frame.dispose();
                    break;
                }
            }
        });
    }

    private void resetBallAttached() {
        double cx;
        double py;

        if (rightActive && !leftActive) {
            cx = paddleRight.x + paddleRight.w / 2.0;
            py = paddleRight.y;
        } else if (leftActive && !rightActive) {
            cx = paddleLeft.x + paddleLeft.w / 2.0;
            py = paddleLeft.y;
        } else {
            cx = (paddleLeft.x + paddleLeft.w / 2 + paddleRight.x + paddleRight.w / 2) / 2.0;
            py = Math.min(paddleLeft.y, paddleRight.y);
        }

        ball = new Ball(cx - 4, py + 20, 8);
        ball.vx = 0; ball.vy = 0; started = false;
    }

    private void resetAll() {
        score = 0;
        level = 1;
        currentBallSpeed = BALL_SPEED;

        choosePatternForLevel();
        createBricksByLevel();

        double initialY = bottom + 40;
        if (leftActive) paddleLeft.y = initialY;
        if (rightActive) paddleRight.y = initialY;

        lives = maxLives;
        resetBallAttached();

        // إعادة تعيين المؤقت
        timerActive = false;
        levelStartTimeMillis = 0;
        timeElapsedSeconds = 0;
    }

    private void startBall() {
        ball.vx = (Math.random() > 0.5 ? 1 : -1) * currentBallSpeed;
        ball.vy = currentBallSpeed;
        started = true;

        // بدء المؤقت عند إطلاق الكرة
        if (!timerActive) {
            levelStartTimeMillis = System.currentTimeMillis();
            timerActive = true;
            timeElapsedSeconds = 0;
        }
    }

    public void setLevel(int level) {
        if (level < 1) level = 1;
        this.level = level;
    }

    public void setPlayers(int players) {
        if (players < 1) players = 1;
        if (players > 2) players = 2;
        this.players = players;

        if (players == 1) {
            rightActive = true;
            leftActive = false;
        } else {
            leftActive = true;
            rightActive = true;
        }
    }

    // ----- KeyListener -----
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT: leftArrow = true; break;
            case KeyEvent.VK_RIGHT: rightArrow = true; break;
            case KeyEvent.VK_UP: upArrow = true; break;
            case KeyEvent.VK_DOWN: downArrow = true; break;
            case KeyEvent.VK_A: aKey = true; break;
            case KeyEvent.VK_D: dKey = true; break;
            case KeyEvent.VK_W: wKey = true; break;
            case KeyEvent.VK_S: sKey = true; break;
            case KeyEvent.VK_ENTER:
                if (!started && !gameOver) startBall();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT: leftArrow = false; break;
            case KeyEvent.VK_RIGHT: rightArrow = false; break;
            case KeyEvent.VK_UP: upArrow = false; break;
            case KeyEvent.VK_DOWN: downArrow = false; break;
            case KeyEvent.VK_A: aKey = false; break;
            case KeyEvent.VK_D: dKey = false; break;
            case KeyEvent.VK_W: wKey = false; break;
            case KeyEvent.VK_S: sKey = false; break;
        }
    }

    @Override public void keyTyped(KeyEvent e) {}

    // ----- MouseListener / MouseMotionListener (مصادر جاهزة للتوسع) -----
    @Override public void mouseClicked(java.awt.event.MouseEvent e) {}
    @Override public void mousePressed(java.awt.event.MouseEvent e) {}
    @Override public void mouseReleased(java.awt.event.MouseEvent e) {}
    @Override public void mouseEntered(java.awt.event.MouseEvent e) {}
    @Override public void mouseExited(java.awt.event.MouseEvent e) {}
    @Override public void mouseDragged(java.awt.event.MouseEvent e) {}
    @Override public void mouseMoved(java.awt.event.MouseEvent e) {}

    // ----- Helpers -----
    private void attachBallAboveActivePaddle() {
        double centerX;
        double attachY;
        if (rightActive && !leftActive) {
            centerX = paddleRight.x + paddleRight.w / 2.0;
            attachY = paddleRight.y;
        } else if (leftActive && !rightActive) {
            centerX = paddleLeft.x + paddleLeft.w / 2.0;
            attachY = paddleLeft.y;
        } else {
            centerX = (paddleLeft.x + paddleLeft.w / 2 + paddleRight.x + paddleRight.w / 2) / 2.0;
            attachY = Math.min(paddleLeft.y, paddleRight.y);
        }
        ball.x = centerX - ball.size / 2;
        ball.y = attachY + 20;
    }

    // ----- Inner classes -----
    static class Paddle {
        double x, y, w, h;
        Paddle(double x, double y, double w, double h) {
            this.x = x; this.y = y; this.w = w; this.h = h;
        }
        void moveX(double delta) { this.x += delta; }
        void moveY(double delta) { this.y += delta; }
        void centerAt(double cx) { this.x = cx - this.w / 2.0; }
        boolean isVisible() { return this.w > 0; }
    }

    static class Ball {
        double x, y, size, vx, vy;
        Ball(double x, double y, double size) {
            this.x = x; this.y = y; this.size = size;
        }
        boolean intersects(Paddle p) {
            return x < p.x + p.w && x + size > p.x
                    && y < p.y + p.h && y + size > p.y;
        }
        boolean intersects(Brick b) {
            return x < b.x + b.w && x + size > b.x
                    && y < b.y + b.h && y + size > b.y;
        }
        double centerX() { return x + size / 2; }
    }

    static class Brick {
        double x, y, w, h;
        Brick(double x, double y, double w, double h) {
            this.x = x; this.y = y; this.w = w; this.h = h;
        }
    }
}
