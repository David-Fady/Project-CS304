package com;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

// Change it --V
public class GLCanvasProject implements GLEventListener {
    // ------------------ data type -----------------------------


    // ----------------------- basic --------------------------
    public void init(GLAutoDrawable glAutoDrawable) {
        GL gl = glAutoDrawable.getGL();
        // color canvas
        gl.glClearColor(0.0f, 0.5f, 1.0f, 1.0f);
        // 2D or 3D
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        // size of canvas
        gl.glOrtho(-225, 225, -150, 150,-1,1);
    }
    public void display(GLAutoDrawable glAutoDrawable) {
        GL gl = glAutoDrawable.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);


    }
    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int i2, int i3) {

    }
    public void displayChanged(GLAutoDrawable glAutoDrawable, boolean b, boolean b1) {

    }

    // ------------------------other method ------------------------------------
    private void drawCircle(GL gl, double centerX, double centerY, double radius, float r, float g, float b) {
        gl.glColor3f(r, g, b);
        gl.glBegin(GL.GL_POLYGON);
        for (int i = 0; i <= 360; i++) {
            double ang = Math.toRadians(i);
            double x = Math.cos(ang) * radius + centerX;
            double y = Math.sin(ang) * radius + centerY;
            gl.glVertex2d(x, y);
        }
        gl.glEnd();
    }
    private void drawEmptyCircle(GL gl, double centerX, double centerY, double radius, float r, float g, float b) {
        gl.glColor3f(r, g, b);
        gl.glBegin(GL.GL_LINE_LOOP);
        for (int i = 0; i <= 360; i++) {
            double ang = Math.toRadians(i);
            double x = Math.cos(ang) * radius + centerX;
            double y = Math.sin(ang) * radius + centerY;
            gl.glVertex2d(x, y);
        }
        gl.glEnd();
    }
    private void drawRectangle(GL gl, double x1, double y1, double x2, double y2, float r, float g, float b) {
        gl.glColor3f(r, g, b);
        gl.glBegin(GL.GL_POLYGON);
        gl.glVertex2d(x1, y2);
        gl.glVertex2d(x2, y2);
        gl.glVertex2d(x2, y1);
        gl.glVertex2d(x1, y1);
        gl.glEnd();
    }
    private void drawTriangle(GL gl, double x1, double y1, double x2, double y2, double x3, double y3, float r, float g, float b) {
        gl.glColor3f(r, g, b);
        gl.glBegin(GL.GL_POLYGON);
        gl.glVertex2d(x1, y1);
        gl.glVertex2d(x2, y2);
        gl.glVertex2d(x3, y3);
        gl.glEnd();
    }
    private void drawRectangle_WAndH(GL gl, double x, double y, double width, double height, float r, float g, float b) {
        gl.glColor3f(r, g, b);
        gl.glBegin(GL.GL_POLYGON);
        gl.glVertex2d(x, y);
        gl.glVertex2d(x + width, y);
        gl.glVertex2d(x + width, y + height);
        gl.glVertex2d(x, y + height);
        gl.glEnd();
    }
    private void drawTriangle_WAndH(GL gl, double x, double y, double base, double height, float r, float g, float b) {
        gl.glColor3f(r, g, b);
        gl.glBegin(GL.GL_POLYGON);
        gl.glVertex2d(x, y);
        gl.glVertex2d(x + base, y);
        gl.glVertex2d(x + base / 2, y + height);
        gl.glEnd();

    }
    private void drawLine(GL gl, double x1, double y1, double x2, double y2, double width, float r, float g, float b) {
        gl.glColor3f(r, g, b);
        gl.glLineWidth((float) width);
        gl.glBegin(GL.GL_LINES);
        gl.glVertex2d(x1, y1);
        gl.glVertex2d(x2, y2);
        gl.glEnd();
    }
    private void drawPoint(GL gl, double x, double y, double size , float r, float g, float b) {
        gl.glColor3f(r, g, b);
        gl.glPointSize((float) size);
        gl.glBegin(GL.GL_LINES);
        gl.glVertex2d(x, y);
        gl.glEnd();
    }
    private void drawEllipse(GL gl, double centerX, double centerY, double radiusX, double radiusY, float r, float g, float b) {
        gl.glColor3f(r, g, b);
        gl.glBegin(GL.GL_POLYGON);
        for (int i = 0; i <= 360; i++) {
            double ang = Math.toRadians(i);
            double x = Math.cos(ang) * radiusX + centerX;
            double y = Math.sin(ang) * radiusY + centerY;
            gl.glVertex2d(x, y);
        }
        gl.glEnd();
    }

    //--------------------------------------------------------------------------






}
