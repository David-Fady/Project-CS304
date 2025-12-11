package com;

import com.sun.opengl.util.FPSAnimator;
import javax.media.opengl.GLCanvas;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class Project extends JFrame  {
    public static void main(String[] er) {
        GameWindow.main(er);
    }

    //------------------DataField--------------------
    GLCanvas canvasT;
    GLCanvasProject lo = new GLCanvasProject();
    FPSAnimator ani;

    //------------------Constructor--------------------
    public Project() {

        // create JFrame
        this.setTitle("Brick Breaker");
        this.setSize(900, 650);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // create GLCanvas
        canvasT = new GLCanvas();
        canvasT.addGLEventListener(lo);

        // add the GLCanvas to JFrame
        this.add(canvasT, BorderLayout.CENTER);

        // create Animator.
        ani = new FPSAnimator(canvasT, 30);

        // add Canvas to Animator.
        ani.start();

        //save
        this.setVisible(true);

        // request Focus to CanvasT (from Mouse Motion Listener , JText , ... )
        canvasT.requestFocusInWindow();

    }

}
