package com;

import com.sun.opengl.util.j2d.TextRenderer;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


public class GLCanvasProject implements GLEventListener, KeyListener {
    // -------------------- dataFiled ------------------

    // world bounds match original
    private double left = -225, right = 225, bottom = -150, top = 150;

    // paddles (two bottom paddles side-by-side)
    private Paddle paddleLeft, paddleRight;

    // ball
    private Ball ball;
    private boolean started = false; // ball starts when any paddle moves

    // bricks
    private List<Brick> bricks = new ArrayList<>();

    // shared score
    private int score = 0;

    // movement flags for smooth movement
    private boolean leftArrow, rightArrow, aKey, dKey;

    //
    private TextRenderer textRenderer;

    //-------------------- Basic ------------------
    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL gl = glAutoDrawable.getGL();
        gl.glClearColor(0.0f, 0.5f, 1.0f, 1.0f);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(left, right, bottom, top,-1,1);
        //------------------------------------------------

        // paddles sizes and positions (side-by-side)
        double paddleW = 90, paddleH = 12;
        double y = bottom + 20; // near bottom
        // place left paddle at -100, right paddle next to it
        paddleLeft = new Paddle(-110, y, paddleW, paddleH);
        paddleRight = new Paddle(20, y, paddleW, paddleH);

        // ball starts above paddles middle
        resetBallAttached();

        // create bricks grid above
        createBricks();

        //
        textRenderer = new TextRenderer(new Font("Arial", Font.BOLD, 16));
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        GL gl = glAutoDrawable.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        //-------------------------------------------

        // update input-driven movements
        updatePaddles();

        // update ball only if started
        if(started){
            updateBall();
        } else {
            // keep ball attached above paddles (in case they moved before starting)
            double centerX = (paddleLeft.x + paddleLeft.w/2 + paddleRight.x + paddleRight.w/2) / 2.0;
            ball.x = centerX - ball.size/2;
            ball.y = paddleLeft.y + 20;
        }

        // draw bricks
        for(Brick b : bricks)
            drawBeautifulBrick(gl, b.x, b.y, b.w, b.h);

        // draw paddles
        drawRectangle(gl, paddleLeft.x, paddleLeft.y, paddleLeft.x + paddleLeft.w, paddleLeft.y + paddleLeft.h, 0.0f, 0.3f, 0.9f);
        drawRectangle(gl, paddleRight.x, paddleRight.y, paddleRight.x + paddleRight.w, paddleRight.y + paddleRight.h, 0.7f, 0.1f, 0.1f);

        // draw ball
        drawCircle(gl, ball.x + ball.size/2, ball.y + ball.size/2, ball.size, 1f, 0.9f, 0.0f);

        // draw score (simple rectangles as digits substitute) -- draw as text using Swing overlay is more work,
        // so use a simple rectangle + text via JOptionPane not ideal; instead draw a simple score bar made of small squares
        drawScore(gl);

        // Your Score.
        textRenderer.beginRendering(500, 300); //  (width, height)
        textRenderer.setColor(1.0f, 1.0f, 0f, 1.0f); // color
        textRenderer.draw("Score: " + score, 10, 10); //  x,y
        textRenderer.endRendering();
    }
    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int i2, int i3) {
    }
    @Override
    public void displayChanged(GLAutoDrawable glAutoDrawable, boolean b, boolean b1) {
    }

    // ------------------ drawing method ------------------
    private void drawRectangle(GL gl, double x1, double y1, double x2, double y2, float r, float g, float b) {
        gl.glColor3f(r, g, b);
        gl.glBegin(GL.GL_POLYGON);
        gl.glVertex2d(x1, y2);
        gl.glVertex2d(x2, y2);
        gl.glVertex2d(x2, y1);
        gl.glVertex2d(x1, y1);
        gl.glEnd();
    }
    private void drawCircle(GL gl, double centerX, double centerY, double radius, float r, float g, float b) {
        gl.glColor3f(r, g, b);
        gl.glBegin(GL.GL_POLYGON);
        int steps = 24;
        for (int i = 0; i < steps; i++) {
            double ang = 2*Math.PI*i/steps;
            double x = Math.cos(ang) * radius + centerX;
            double y = Math.sin(ang) * radius + centerY;
            gl.glVertex2d(x, y);
        }
        gl.glEnd();
    }
    private void drawBeautifulBrick(GL gl, double x, double y, double w, double h) {
        // Border Color (Dark Brown)
        float br = 0.35f, bg = 0.16f, bb = 0.05f;

        // Main Gradient Colors
        float topR = 1.0f, topG = 0.95f, topB = 0.85f;   // Light Cream
        float botR = 0.90f, botG = 0.80f, botB = 0.70f;  // Slight darker

        // Draw border (outer frame)
        gl.glColor3f(br, bg, bb);
        gl.glBegin(GL.GL_POLYGON);
        gl.glVertex2d(x , y);
        gl.glVertex2d(x + w , y);
        gl.glVertex2d(x + w ,y + h);
        gl.glVertex2d(x,y + h);
        gl.glEnd();

        double t = 3; // border thickness

        // ----- Gradient fill -----
        gl.glBegin(GL.GL_POLYGON);

        // top-left
        gl.glColor3f(topR, topG, topB);
        gl.glVertex2d(x + t, y + h - t);

        // top-right
        gl.glColor3f(topR, topG, topB);
        gl.glVertex2d(x + w - t, y + h - t);

        // bottom-right
        gl.glColor3f(botR, botG, botB);
        gl.glVertex2d(x + w - t, y + t);

        // bottom-left
        gl.glColor3f(botR, botG, botB);
        gl.glVertex2d(x + t, y + t);

        gl.glEnd();

        // ----- Top highlight line -----
        gl.glLineWidth(2);
        gl.glColor3f(1f, 1f, 1f); // white highlight
        gl.glBegin(GL.GL_LINES);
        gl.glVertex2d(x + t,     y + h - t + 1);
        gl.glVertex2d(x + w - t, y + h - t + 1);
        gl.glEnd();

        // ----- Bottom shadow line -----
        gl.glColor3f(0.25f, 0.12f, 0.02f); // dark shadow
        gl.glBegin(GL.GL_LINES);
        gl.glVertex2d(x + t,     y + t - 1);
        gl.glVertex2d(x + w - t, y + t - 1);
        gl.glEnd();
    }

    // ----------------------- other method ------------------
    private void createBricks(){
        bricks.clear();
        int rows = 4, cols = 9;
        double brickW = 40, brickH = 18;
        double startX = left + 30;
        double startY = 40;
        for(int r=0;r<rows;r++){
            for(int c=0;c<cols;c++){
                double x = startX + c*(brickW);
                double y = startY + r*(brickH);
                bricks.add(new Brick(x,y,brickW,brickH));
            }
        }
    }

    private void drawScore(GL gl){
        String s = "Score: " + score;
    }

    private void updatePaddles(){
        double speed = 2.5;
        if(leftArrow) paddleRight.x -= speed; // arrows control right paddle
        if(rightArrow) paddleRight.x += speed;
        if(aKey) paddleLeft.x -= speed; // A/D control left paddle
        if(dKey) paddleLeft.x += speed;

        // clamp
        clampPaddle(paddleLeft);
        clampPaddle(paddleRight);
    }

    private void clampPaddle(Paddle p){
        if(p.x < left) p.x = left;
        if(p.x + p.w > right) p.x = right - p.w;
    }

    private void updateBall(){
        // basic physics
        ball.x += ball.vx;
        ball.y += ball.vy;

        // wall collision
        if(ball.x < left) { ball.x = left; ball.vx = -ball.vx; }
        if(ball.x + ball.size > right) { ball.x = right - ball.size; ball.vx = -ball.vx; }
        if(ball.y + ball.size > top) { ball.y = top - ball.size; ball.vy = -Math.abs(ball.vy); }

        // collision with paddles
        if(ball.intersects(paddleLeft) && ball.vy < 0){
            ball.vy = Math.abs(ball.vy);
            double hit = (ball.centerX() - (paddleLeft.x + paddleLeft.w/2)) / (paddleLeft.w/2);
            ball.vx += hit * 1.2;
        }
        if(ball.intersects(paddleRight) && ball.vy < 0){
            ball.vy = Math.abs(ball.vy);
            double hit = (ball.centerX() - (paddleRight.x + paddleRight.w/2)) / (paddleRight.w/2);
            ball.vx += hit * 1.2;
        }

        // bricks collision
        Iterator<Brick> it = bricks.iterator();
        while(it.hasNext()){
            Brick b = it.next();
            if(ball.intersects(b)){
                it.remove();
                score++;
                Toolkit.getDefaultToolkit().beep();
                ball.vy = -ball.vy;
                System.out.println("Brick hit! Score=" + score);
                break;
            }
        }

        checkWin();
        checkLost();

    }

    private void checkWin(){
        if(bricks.isEmpty()){
            ball.vx = 0;
            ball.vy = 0;
            started = false;

            final int finalScore = score;

            EventQueue.invokeLater(() -> {
                String[] options = {"Restart", "Exit"};
                int result = JOptionPane.showOptionDialog(
                        null,
                        "You won!",
                        "Victory",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        options,
                        options[0]
                );
                if(result == 0){
                    resetAll();
                } else {
                    System.exit(0);
                }
            });
        }
    }
    private void checkLost(){
        if(ball.y + ball.size < bottom){
            // stop the ball
            ball.vx = 0;
            ball.vy = 0;
            started = false;

            // show dialog in EDT
            EventQueue.invokeLater(() -> {
                String[] options = {"Restart", "Exit"};
                int result = JOptionPane.showOptionDialog(
                        null,
                        "You lost!",
                        "Game Over",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        options,
                        options[0]
                );

                if(result == 0){
                    // restart
                    resetAll();
                } else {
                    // exit
                    System.exit(0);
                }
            });
        }

    }

    private void resetBallAttached(){
        // ball sits between paddles initially
        double centerX = (paddleLeft.x + paddleLeft.w/2 + paddleRight.x + paddleRight.w/2) / 2.0;
        ball = new Ball(centerX, paddleLeft.y + 20, 8);
        ball.vx = 0; ball.vy = 0;
        started = false;
    }

    private void resetAll(){
        score = 0;
        createBricks();
        resetBallAttached();
    }

    // ------------------ (KeyListener) ------------------
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        switch(code){
            case KeyEvent.VK_LEFT: leftArrow = true; break;
            case KeyEvent.VK_RIGHT: rightArrow = true; break;
            case KeyEvent.VK_A: aKey = true; break;
            case KeyEvent.VK_D: dKey = true; break;
            case KeyEvent.VK_SPACE: if(!started) startBall(); break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        switch(code){
            case KeyEvent.VK_LEFT: leftArrow = false; break;
            case KeyEvent.VK_RIGHT: rightArrow = false; break;
            case KeyEvent.VK_A: aKey = false; break;
            case KeyEvent.VK_D: dKey = false; break;
        }
    }

    private void startBall(){
        if(started) return;
        // give an initial velocity
        ball.vx = 2.5 * (Math.random() > 0.5 ? 1 : -1);
        ball.vy = -4.0; // going up (negative y is down in our ortho? we used bottom < top so vy positive moves up)
        // adjust: we want ball to go up, so vy positive
        ball.vy = 4.0;
        started = true;
    }

    // ------------------ other classes ------------------
    static class Paddle {
        double x,y,w,h;
        Paddle(double x,double y,double w,double h){this.x=x;this.y=y;this.w=w;this.h=h;}
    }

    static class Ball{
        double x,y,size,vx,vy;
        Ball(double x,double y,double size){this.x=x;this.y=y;this.size=size;this.vx=0;this.vy=0;}
        boolean intersects(Paddle p){
            return x < p.x + p.w && x + size > p.x && y < p.y + p.h && y + size > p.y;
        }
        boolean intersects(Brick b){
            return x < b.x + b.w && x + size > b.x && y < b.y + b.h && y + size > b.y;
        }
        double centerX(){return x + size/2;}
    }

    static class Brick{
        double x,y,w,h;
        Brick(double x,double y,double w,double h){this.x=x;this.y=y;this.w=w;this.h=h;}
    }
}
