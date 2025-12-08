package com;

import com.sun.opengl.util.FPSAnimator;
import javax.media.opengl.GLCanvas;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class Project extends JFrame implements ActionListener, KeyListener, MouseListener, MouseMotionListener {
    public static void main(String[] er) {
        EventQueue.invokeLater(() -> new Project());
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


        // add Key Listener to GLCanvas
        canvasT.addKeyListener(lo);
        // add Mouse Listener to GLCanvas
        canvasT.addMouseListener(this);
        // add Mouse Motion Listener to GLCanvas
        canvasT.addMouseMotionListener(this);


        // create Animator.
        ani = new FPSAnimator(canvasT, 60);


        // add Canvas to Animator.
        ani.start();


        //save
        this.setVisible(true);


        // request Focus to CanvasT (from Mouse Motion Listener , JText , ... )
        canvasT.requestFocusInWindow();


    }


    // ------------------ ActionListener ---------------------
    public void actionPerformed(ActionEvent e) {
    }

    // ------------------ KeyListener ---------------------
    public void keyTyped(KeyEvent e) {

    }
    public void keyPressed(KeyEvent e) {

    }
    public void keyReleased(KeyEvent e) {

    }


    // ------------------ MouseListener ---------------------
    public void mouseClicked(MouseEvent e) {

    }
    public void mousePressed(MouseEvent e) {

    }
    public void mouseReleased(MouseEvent e) {

    }
    public void mouseEntered(MouseEvent e) {

    }
    public void mouseExited(MouseEvent e) {

    }

    // ------------------ MouseMotionListener ---------------------
    public void mouseDragged(MouseEvent e) {

    }
    public void mouseMoved(MouseEvent e) {

    }
}





























