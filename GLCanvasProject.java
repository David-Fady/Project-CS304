package com.CS304.TestPro;

import com.sun.opengl.util.j2d.TextRenderer;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

public class GLCanvasProject implements GLEventListener, KeyListener {

    private double left = -225, right = 225, bottom = -150, top = 150;

    private Paddle paddleLeft, paddleRight;
    private Ball ball;
    private boolean started = false;
    private List<Brick> bricks = new ArrayList<>();
    private int score = 0;

    private int level = 1; // 🔥 مستوى اللعبة
    private double currentBallSpeed = 3.0;

    // pattern index (1..6)
    private int patternIndex = 1;

    private Random rand = new Random();

    private boolean leftArrow, rightArrow, aKey, dKey, upArrow, wKey, downArrow, sKey;

    private TextRenderer textRenderer;

    private final double PADDLE_SPEED = 3.0;
    private final double BALL_SPEED = 3.0;

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL gl = glAutoDrawable.getGL();

        gl.glClearColor(0.65f, 0.55f, 0.45f, 1.0f);

        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(left, right, bottom, top, -1, 1);

        double paddleW = 90, paddleH = 12;
        double initialY = bottom + 40;

        paddleLeft = new Paddle(-110, initialY, paddleW, paddleH);
        paddleRight = new Paddle(20, initialY, paddleW, paddleH);

        resetBallAttached();
        choosePatternForLevel();
        createBricksByLevel();

        textRenderer = new TextRenderer(new Font("Arial", Font.BOLD, 16));
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        GL gl = glAutoDrawable.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);

        updatePaddles();

        if (!started) {
            double centerX = (paddleLeft.x + paddleLeft.w/2 + paddleRight.x + paddleRight.w/2) / 2.0;
            ball.x = centerX - ball.size / 2;
            ball.y = Math.min(paddleLeft.y, paddleRight.y) + 20;
        } else {
            updateBall();
        }

        // ---- رسم الطوب باللون الخاص بالمستوى ----
        Color levelColor = getColorForLevel(level);

        for (Brick b : bricks) {
            drawBrick(gl, b.x, b.y, b.w, b.h, levelColor);
        }

        drawFancyPaddle(gl, paddleLeft.x, paddleLeft.y,
                paddleLeft.x + paddleLeft.w, paddleLeft.y + paddleLeft.h);

        drawFancyPaddle(gl, paddleRight.x, paddleRight.y,
                paddleRight.x + paddleRight.w, paddleRight.y + paddleRight.h);

        drawCircle(gl, ball.x + ball.size/2, ball.y + ball.size/2,
                ball.size/2, 1f, 0.9f, 0.0f);

        textRenderer.beginRendering(500, 300);
        textRenderer.setColor(1.0f, 1.0f, 0f, 1.0f);
        textRenderer.draw("Score: " + score, 10, 10);
        textRenderer.draw("Level: " + level, 400, 10);
        textRenderer.endRendering();
    }

    @Override public void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int i2, int i3) {}
    @Override public void displayChanged(GLAutoDrawable glAutoDrawable, boolean b, boolean b1) {}

    // --------------------------------------------------

    private void drawBrick(GL gl, double x, double y, double w, double h, Color fillColor) {

        // fill
        gl.glColor3f(fillColor.getRed()/255f, fillColor.getGreen()/255f, fillColor.getBlue()/255f);
        gl.glBegin(GL.GL_POLYGON);
        gl.glVertex2d(x, y);
        gl.glVertex2d(x + w, y);
        gl.glVertex2d(x + w, y + h);
        gl.glVertex2d(x, y + h);
        gl.glEnd();

        // border (always black) - set color explicitly
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

    private void drawCircle(GL gl, double cx, double cy, double radius,
                            float r, float g, float b) {
        gl.glColor3f(r, g, b);
        gl.glBegin(GL.GL_POLYGON);
        int steps = 32;
        for (int i = 0; i < steps; i++) {
            double ang = 2 * Math.PI * i / steps;
            gl.glVertex2d(Math.cos(ang)*radius + cx, Math.sin(ang)*radius + cy);
        }
        gl.glEnd();
    }

    // --------------------------------------------------

    /** 🎯 إنشاء الطوب لكل Level مع اختيار Pattern */
    private void createBricksByLevel() {

        bricks.clear();


        int cols = 12;
        double brickW = 30;
        double brickH = 18;

        double totalWidth = right - left;
        double spacingX = (totalWidth - (cols * brickW)) / (cols + 1);

        int p = patternIndex;

        switch (p) {
            case 1:
            {
                int rows = level + 1;
                double startY = 40;
                for (int r = 0; r < rows; r++) {
                    double y = startY + r * (brickH + 5);
                    for (int c = 0; c < cols; c++) {
                        double x = left + spacingX + c * (brickW + spacingX);
                        bricks.add(new Brick(x, y, brickW, brickH));
                    }
                }
            }
            break;

            case 2:
            {
                int rows = Math.min(level + 2, 8);
                double centerX = (left + right) / 2.0;
                double startY = 60;
                for (int r = 0; r < rows; r++) {
                    int bricksInRow = cols - r*2;
                    if (bricksInRow <= 0) break;
                    double rowWidth = bricksInRow * brickW + (bricksInRow - 1) * 6;
                    double x0 = centerX - rowWidth / 2.0;
                    double y = startY + r * (brickH + 6);
                    for (int c = 0; c < bricksInRow; c++) {
                        double x = x0 + c * (brickW + 6);
                        bricks.add(new Brick(x, y, brickW, brickH));
                    }
                }
            }
            break;

            case 3:
            {
                int rows = level + 2;
                double startY = 50;
                double centerX = (left + right) / 2.0;
                for (int r = 0; r < rows; r++) {
                    double y = startY + r * (brickH + 5);
                    int span = r;
                    for (int i = -span; i <= span; i++) {
                        double xLeft = centerX + i * (brickW + 4) - (brickW/2.0) - (span*(brickW+4))/2.0;
                        bricks.add(new Brick(xLeft, y, brickW, brickH));
                    }
                }
            }
            break;

            case 4:
            {
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
            }
            break;

            case 5:
            {
                double centerX = (left + right) / 2.0;
                double[] relX = { -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5 };
                double[] relY = { 0, 0, 0, -1, -1, -2, -1, -1, 0, 0, 0 };
                int baseRows = level + 1;
                double startY = 60;
                for (int r = 0; r < baseRows; r++) {
                    double y = startY + r * (brickH + 5);
                    for (int i = 0; i < relX.length; i++) {
                        if (Math.abs(relX[i]) + r > 6) continue;
                        double x = centerX + relX[i] * (brickW + 2) - brickW/2.0;
                        bricks.add(new Brick(x, y, brickW, brickH));
                    }
                }
            }
            break;

            case 6:
            default:
            {
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
            }
            break;
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
            case 1: return new Color(139, 69, 19); // Brown
            case 2: return new Color(50, 205, 50); // Green
            case 3: return new Color(65, 105, 225); // Blue
            case 4: return new Color(220, 20, 60); // Red
            default: return new Color(128, 0, 128); // Purple
        }
    }

    // --------------------------------------------------

    private void updatePaddles() {
        if (leftArrow) paddleRight.x -= PADDLE_SPEED;
        if (rightArrow) paddleRight.x += PADDLE_SPEED;
        if (aKey) paddleLeft.x -= PADDLE_SPEED;
        if (dKey) paddleLeft.x += PADDLE_SPEED;
        if (upArrow) paddleRight.y += PADDLE_SPEED;
        if (wKey) paddleLeft.y += PADDLE_SPEED;
        if (downArrow) paddleRight.y -= PADDLE_SPEED;
        if (sKey) paddleLeft.y -= PADDLE_SPEED;

        clampPaddle(paddleLeft);
        clampPaddle(paddleRight);
    }

    private void clampPaddle(Paddle p) {
        if (p.x < left) p.x = left;
        if (p.x + p.w > right) p.x = right - p.w;

        double maxPaddleY = bottom + (top - bottom) * 0.25;
        if (p.y + p.h > maxPaddleY) p.y = maxPaddleY - p.h;

        if (p.y < bottom + 10) p.y = bottom + 10;
    }

    private void updateBall() {
        ball.x += ball.vx;
        ball.y += ball.vy;

        if (ball.x < left) { ball.x = left; ball.vx = Math.abs(ball.vx); }
        if (ball.x + ball.size > right) { ball.x = right - ball.size; ball.vx = -Math.abs(ball.vx); }
        if (ball.y + ball.size > top) { ball.y = top - ball.size; ball.vy = -Math.abs(ball.vy); }

        handlePaddleCollision(paddleLeft);
        handlePaddleCollision(paddleRight);

        Iterator<Brick> it = bricks.iterator();
        while (it.hasNext()) {
            Brick b = it.next();
            if (ball.intersects(b)) {
                it.remove();
                score++;
                Toolkit.getDefaultToolkit().beep();

                ball.vy = -ball.vy;
                break;
            }
        }

        checkLevelComplete();
        checkLost();
    }

    private void handlePaddleCollision(Paddle p) {
        if (ball.intersects(p)) {
            double hit = (ball.centerX() - (p.x + p.w / 2)) / (p.w / 2);
            ball.vx = hit * currentBallSpeed;

            if (Math.abs(ball.vx) < 1.5) ball.vx = 1.5 * (ball.vx > 0 ? 1 : -1);

            ball.vy = Math.abs(ball.vy) + 0.2;
            ball.y = p.y + p.h;
        }
    }

    private void checkLevelComplete() {
        if (bricks.isEmpty()) {

            level++;
            currentBallSpeed += 0.5;
            started = false;

            JOptionPane.showMessageDialog(null,
                    "Level " + (level-1) + " Completed!\nNext Level: " + level);

            choosePatternForLevel();
            createBricksByLevel();
            resetBallAttached();
        }
    }

    private void checkLost() {
        if (ball.y + ball.size < bottom) {

            JOptionPane.showMessageDialog(null,
                    "You Lost!\nFinal Score: " + score);

            resetAll();
        }
    }

    private void resetBallAttached() {
        double cx = (paddleLeft.x + paddleLeft.w/2 + paddleRight.x + paddleRight.w/2) / 2;
        double py = Math.min(paddleLeft.y, paddleRight.y);
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
        paddleLeft.y = initialY;
        paddleRight.y = initialY;

        resetBallAttached();
    }

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
            case KeyEvent.VK_SPACE:
                if (!started) startBall();
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

    private void startBall() {
        ball.vx = (Math.random() > 0.5 ? 1 : -1) * currentBallSpeed;
        ball.vy = currentBallSpeed;
        started = true;
    }

    // --------------------------------------------------

    static class Paddle {
        double x, y, w, h;
        Paddle(double x, double y, double w, double h) {
            this.x = x; this.y = y; this.w = w; this.h = h;
        }
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
