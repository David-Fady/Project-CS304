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
import java.util.Iterator;
import javax.swing.JOptionPane;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;
import java.net.URL;

public class GLCanvasProject implements GLEventListener, KeyListener,
        java.awt.event.MouseListener, java.awt.event.MouseMotionListener {

    public interface GameEndListener {
        void onReturnToMenuRequest();
    }
    private GameEndListener menuListener;

    public void setMenuListener(GameEndListener listener) {
        this.menuListener = listener;
    }

    private double left = -225, right = 225, bottom = -150, top = 150;

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
    private SoundManager soundManager;

    private final double PADDLE_SPEED = 3.0;
    private final double BALL_SPEED = 3.0;

    private int players = 1;

    private boolean levelCompleted = false;
    private boolean gameOver = false;

    private boolean leftActive = false;
    private boolean rightActive = true;

    private int lives = 3;
    private final int maxLives = 3;

    private boolean isPaused = false;

    private long levelStartTimeMillis = 0;
    private long timeElapsedSeconds = 0;
    private boolean timerActive = false;
    private long timeToCompleteLevel = 0;

    private Texture backgroundTexture;
    private boolean backgroundLoaded = false;

    public GLCanvasProject() {
        soundManager = new SoundManager();
    }

    public void setPaused(boolean paused) {
        this.isPaused = paused;

        if (paused) {
            timerActive = false;
        } else if (started && !gameOver && !levelCompleted) {
            levelStartTimeMillis = System.currentTimeMillis() - (timeElapsedSeconds * 1000);
            timerActive = true;
        }
        resetInputStates();
    }

    public boolean isPaused() {
        return isPaused;
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL gl = glAutoDrawable.getGL();
        gl.glClearColor(0.65f, 0.55f, 0.45f, 1.0f);

        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(left, right, bottom, top, -1, 1);

        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        loadBackgroundTexture(gl);

        double paddleW = 90, paddleH = 12;
        double initialY = bottom + 40;

        paddleRight = new Paddle(20, initialY, paddleW, paddleH);
        if (leftActive) {
            paddleLeft = new Paddle(-110, initialY, paddleW, paddleH);
        } else {
            paddleLeft = new Paddle(left - 500, initialY, paddleW, paddleH);
        }

        resetBallAttached();
        choosePatternForLevel();
        createBricksByLevel();

        textRenderer = new TextRenderer(new Font("Arial", Font.BOLD, 16));

        lives = maxLives;
        resetInputStates();
    }

    private void resetInputStates() {
        leftArrow = false; rightArrow = false; upArrow = false; downArrow = false;
        aKey = false; dKey = false; wKey = false; sKey = false;
    }

    private void loadBackgroundTexture(GL gl) {
        try {
            String[] possiblePaths = {
                    "/com/Image/game_background.jpg", "/com/Image/game_background.png",
                    "/com/Image/background.jpg", "/com/Image/background.png",
                    "game_background.jpg", "game_background.png"
            };

            for (String path : possiblePaths) {
                try {
                    URL textureUrl = getClass().getResource(path);
                    if (textureUrl != null) {
                        backgroundTexture = TextureIO.newTexture(textureUrl, true, null);
                        backgroundLoaded = true;
                        break;
                    }
                } catch (Exception e) { continue; }
            }

            if (!backgroundLoaded) {
                System.err.println("Could not load background texture. Using solid color.");
            }
        } catch (Exception e) {
            System.err.println("Error loading background texture: " + e.getMessage());
            backgroundLoaded = false;
        }
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        GL gl = glAutoDrawable.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        drawBackground(gl);

        if (!isPaused) {
            updatePaddles();
            if (!started) {
                attachBallAboveActivePaddle();
            } else {
                updateBall();
            }
        }

        Color levelColor = getColorForLevel(level);

        for (Brick b : bricks) {
            drawBrick(gl, b.x, b.y, b.w, b.h, levelColor);
        }

        if (leftActive) {
            drawFancyPaddle(gl, paddleLeft.x, paddleLeft.y, paddleLeft.x + paddleLeft.w, paddleLeft.y + paddleLeft.h);
        }
        if (rightActive) {
            drawFancyPaddle(gl, paddleRight.x, paddleRight.y, paddleRight.x + paddleRight.w, paddleRight.y + paddleRight.h);
        }

        drawCircle(gl, ball.x + ball.size / 2, ball.y + ball.size / 2, ball.size / 2, 1f, 0.9f, 0.0f);

        textRenderer.beginRendering(500, 300);
        textRenderer.setColor(1.0f, 1.0f, 0f, 1.0f);
        textRenderer.draw("Score: " + score, 10, 10);
        textRenderer.draw("Level: " + level, 400, 10);

        if (!gameOver) {
            textRenderer.setColor(1.0f, 1.0f, 0f, 1.0f);
            textRenderer.draw("Time Elapsed: " + timeElapsedSeconds + "s", 200, 10);

            if (!started) {
                textRenderer.draw("Press ENTER to Start", 160, 110);
            } else if (isPaused) {
                textRenderer.draw("GAME PAUSED", 180, 110);
            }
        }
        textRenderer.endRendering();

        drawLives(gl);
    }

    private void drawBackground(GL gl) {
        if (backgroundLoaded && backgroundTexture != null) {
            backgroundTexture.enable();
            backgroundTexture.bind();
            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            gl.glColor4f(1.0f, 1.0f, 1.0f, 0.7f);
            gl.glBegin(GL.GL_QUADS);
            gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2d(left, bottom);
            gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2d(right, bottom);
            gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2d(right, top);
            gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2d(left, top);
            gl.glEnd();
            backgroundTexture.disable();
        } else {
            gl.glColor3f(0.3f, 0.4f, 0.5f);
            gl.glBegin(GL.GL_QUADS);
            gl.glVertex2d(left, bottom); gl.glVertex2d(right, bottom);
            gl.glVertex2d(right, top); gl.glVertex2d(left, top);
            gl.glEnd();
        }
        gl.glColor3f(1.0f, 1.0f, 1.0f);
    }

    @Override public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {
        GL gl = d.getGL();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(left, right, bottom, top, -1, 1);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }
    @Override public void displayChanged(GLAutoDrawable d, boolean b, boolean c) {}

    private void drawBrick(GL gl, double x, double y, double w, double h, Color fillColor) {
        gl.glColor4f(fillColor.getRed()/255f, fillColor.getGreen()/255f, fillColor.getBlue()/255f, 0.9f);
        gl.glBegin(GL.GL_POLYGON);
        gl.glVertex2d(x, y); gl.glVertex2d(x + w, y);
        gl.glVertex2d(x + w, y + h); gl.glVertex2d(x, y + h);
        gl.glEnd();

        gl.glLineWidth(3.5f);
        gl.glColor3f(0f, 0f, 0f);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2d(x, y); gl.glVertex2d(x + w, y);
        gl.glVertex2d(x + w, y + h); gl.glVertex2d(x, y + h);
        gl.glEnd();
    }

    private void drawFancyPaddle(GL gl, double x1, double y1, double x2, double y2) {
        double shadowOffset = -4;
        gl.glColor4f(0f, 0f, 0f, 0.35f);
        gl.glBegin(GL.GL_POLYGON);
        gl.glVertex2d(x1 + 3, y1 + shadowOffset); gl.glVertex2d(x2 + 3, y1 + shadowOffset);
        gl.glVertex2d(x2 + 3, y2 + shadowOffset); gl.glVertex2d(x1 + 3, y2 + shadowOffset);
        gl.glEnd();

        gl.glBegin(GL.GL_POLYGON);
        gl.glColor4f(0.0f, 0.3f, 0.8f, 0.9f); gl.glVertex2d(x1, y2); gl.glVertex2d(x2, y2);
        gl.glColor4f(0.3f, 0.6f, 1.0f, 0.9f); gl.glVertex2d(x2, y1); gl.glVertex2d(x1, y1);
        gl.glEnd();

        gl.glLineWidth(4f);
        gl.glColor3f(0f, 0f, 0f);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2d(x1, y2); gl.glVertex2d(x2, y2);
        gl.glVertex2d(x2, y1); gl.glVertex2d(x1, y1);
        gl.glEnd();
    }

    private void drawCircle(GL gl, double cx, double cy, double radius, float r, float g, float b) {
        gl.glColor3f(r, g, b);
        gl.glBegin(GL.GL_POLYGON);
        int steps = 32;
        for (int i = 0; i < steps; i++) {
            double t = 2 * Math.PI * i / steps;
            gl.glVertex2d(Math.cos(t) * radius + cx, Math.sin(t) * radius + cy);
        }
        gl.glEnd();
    }

    private void drawLives(GL gl) {
        double size = 9;
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
                        if (rand.nextDouble() < 0.15) continue;
                        double y = startY + r * (brickH + 6);
                        bricks.add(new Brick(x, y, brickW, brickH));
                    }
                }
            } break;

            
            case 5: {
                int rows = 5;
                double startY = 40;

                for (int r = 0; r < rows; r++) {
                    double y = startY + r * (brickH + 4);
                    for (int c = 0; c < cols; c++) {
                        double x = left + spacingX + c * (brickW + spacingX);
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
                        if (rand.nextDouble() < 0.20 + (r * 0.03)) continue;
                        double x = left + spacingX + c * (brickW + spacingX);
                        bricks.add(new Brick(x, y, brickW, brickH));
                    }
                }
            } break;
        }

        if (bricks.isEmpty()) {
            double defaultBrickW = 60;
            double defaultBrickH = 20;
            double defaultX = (left + right) / 2.0 - defaultBrickW / 2.0;
            double defaultY = top - 40;
            bricks.add(new Brick(defaultX, defaultY, defaultBrickW, defaultBrickH));
            System.err.println("LEVEL FALLBACK: Bricks list was empty for level " + level + ". Added a default brick.");
        }

        System.out.println("--- LEVEL " + level + " CREATED --- Total Bricks: " + bricks.size());
    }

    private void choosePatternForLevel() {
        patternIndex = ((level - 1) % 6) + 1;
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

    private void updateBall() {
        ball.x += ball.vx;
        ball.y += ball.vy;

        if (ball.x < left) {
            ball.x = left;
            ball.vx = Math.abs(ball.vx);
            soundManager.playBallBounce();
        }
        if (ball.x + ball.size > right) {
            ball.x = right - ball.size;
            ball.vx = -Math.abs(ball.vx);
            soundManager.playBallBounce();
        }
        if (ball.y + ball.size > top) {
            ball.y = top - ball.size;
            ball.vy = -Math.abs(ball.vy);
            soundManager.playBallBounce();
        }

        if (leftActive) handlePaddleCollision(paddleLeft);
        if (rightActive) handlePaddleCollision(paddleRight);

        if (levelCompleted) return;

        Iterator<Brick> it = bricks.iterator();
        while (it.hasNext()) {
            Brick b = it.next();
            if (ball.intersects(b)) {
                it.remove();
                score++;
                soundManager.playBrickExplode();
                ball.vy = -ball.vy;
                break;
            }
        }

        checkWin();
        checkLost();

        updateTimerAndCheckTimeOut();
    }

    private void handlePaddleCollision(Paddle p) {
        if (ball.intersects(p)) {
            double hit = (ball.centerX() - (p.x + p.w / 2)) / (p.w / 2);
            hit = Math.max(-1.0, Math.min(1.0, hit));

            ball.vx = hit * currentBallSpeed;
            if (Math.abs(ball.vx) < 1.5) ball.vx = 1.5 * (ball.vx > 0 ? 1 : -1);

            soundManager.playBallBounce();
            ball.vy = Math.abs(ball.vy) + 0.2;
            ball.y = p.y + p.h;
        }
    }

    private void updateTimerAndCheckTimeOut() {
        if (started && !gameOver && timerActive) {
            long currentTimeMillis = System.currentTimeMillis();
            timeElapsedSeconds = (currentTimeMillis - levelStartTimeMillis) / 1000;
        }
    }


  
    private void checkWin() {
        if (bricks.isEmpty() && !levelCompleted) {
            levelCompleted = true;
            soundManager.playLevelWin();

            timeToCompleteLevel = timeElapsedSeconds;

            timerActive = false;
            levelStartTimeMillis = 0;

            final int FINAL_LEVEL = 5;

            javax.swing.SwingUtilities.invokeLater(() -> {

                if (level >= FINAL_LEVEL) {
                    gameOver = true;

                    JOptionPane.showMessageDialog(null,
                            "🏆 CONGRATULATIONS! YOU WON THE GAME! 🏆" +
                                    "\nTotal Score: " + score +
                                    "\nTime to complete Level " + FINAL_LEVEL + ": " + timeToCompleteLevel + " seconds.");

                    returnToMenu(); 
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Level " + level + " Completed!" +
                                    "\nTime Taken: " + timeToCompleteLevel + " seconds!");

                    level++;
                    currentBallSpeed += 0.5;
                    started = false;

                    choosePatternForLevel();
                    createBricksByLevel();
                    resetBallAttached();

                    levelCompleted = false; 

                    timeToCompleteLevel = 0;
                    timeElapsedSeconds = 0;

                   
                    resetInputStates();
                }
            });
        }
    }

    private void checkLost() {
        if (ball.y + ball.size < bottom && !gameOver) {
            if (lives > 1) {
                lives--;
                soundManager.playGameLost();
                JOptionPane.showMessageDialog(null, "You lost a life! Lives left: " + lives);

                resetBallAttached();
                gameOver = false;
                started = false;

                timerActive = false;

                resetInputStates();
            } else {
                lives = 0;
                gameOver = true;
                soundManager.playGameLost();

                int choice = JOptionPane.showOptionDialog(null,
                        "You Lost! Game Over.\nYour Score: " + score +
                                "\nTime Elapsed Before Loss: " + timeElapsedSeconds + " seconds.",
                        "Game Over",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
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

        timerActive = false;
        levelStartTimeMillis = 0;
        timeElapsedSeconds = 0;

        resetInputStates();
    }

   
    private void returnToMenu() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            resetAll();
            started = false;
            if (menuListener != null) {
                menuListener.onReturnToMenuRequest();
            } else {
                System.err.println("Error: menuListener is null in GLCanvasProject.");
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

        timerActive = false;
        levelStartTimeMillis = 0;
        timeElapsedSeconds = 0;
        timeToCompleteLevel = 0;

        resetInputStates();
    }

    private void startBall() {
        ball.vx = (Math.random() > 0.5 ? 1 : -1) * currentBallSpeed;
        ball.vy = currentBallSpeed;
        started = true;

        isPaused = false;

        if (!timerActive) {
            levelStartTimeMillis = System.currentTimeMillis() - (timeElapsedSeconds * 1000);
            timerActive = true;
        }
    }

    public void toggleSoundMute() {
        if (soundManager != null) {
            soundManager.toggleMute();
        }
    }

    public void setSoundMuted(boolean muted) {
        if (soundManager != null) {
            soundManager.setMuted(muted);
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

    @Override
    public void keyPressed(KeyEvent e) {
        if (isPaused) return;

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
            case KeyEvent.VK_M:
                toggleSoundMute();
                break;
            case KeyEvent.VK_P:
                if (started && !gameOver) setPaused(!isPaused);
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
    @Override public void mouseClicked(java.awt.event.MouseEvent e) {}
    @Override public void mousePressed(java.awt.event.MouseEvent e) {}
    @Override public void mouseReleased(java.awt.event.MouseEvent e) {}
    @Override public void mouseEntered(java.awt.event.MouseEvent e) {}
    @Override public void mouseExited(java.awt.event.MouseEvent e) {}
    @Override public void mouseDragged(java.awt.event.MouseEvent e) {}
    @Override public void mouseMoved(java.awt.event.MouseEvent e) {}

    private void attachBallAboveActivePaddle() {
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

        ball.x = cx - ball.size / 2;
        ball.y = py + 20;
    }


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
