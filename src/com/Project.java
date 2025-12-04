package com;

import com.sun.opengl.util.FPSAnimator;

import javax.media.opengl.GLCanvas;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Project extends JFrame implements ActionListener, KeyListener, MouseListener, MouseMotionListener {
    public static void main(String[] er){

        Project p = new Project();

    }
    //------------------DataField--------------------
    GLCanvas canvasT;
    GLCanvasProject lo = new GLCanvasProject();
    FPSAnimator ani ;
    JPanel jPanel1;
    JButton zoomIn;
    JButton zoomOut;


    //------------------Constructor--------------------
    public Project() {

        // create JFrame
        this.setTitle(" Lab  ");
        this.setSize(600,350);
        this.setLocationRelativeTo(this);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        // create GLCanvas
        canvasT = new GLCanvas();
        canvasT.addGLEventListener(lo);

        // add the GLCanvas to JFrame
        this.add(canvasT, BorderLayout.CENTER);


        // add Key Listener to GLCanvas
        canvasT.addKeyListener(this);
        // add Mouse Listener to GLCanvas
        canvasT.addMouseListener(this);
        // add Mouse Listener to GLCanvas
        canvasT.addMouseMotionListener(this);


        // create Animator.
        ani = new FPSAnimator(60);

        // add Canvas to Animator.
        ani.add(canvasT);
        ani.start();


        // create button
        //-------------(1)----------------
        zoomIn = new JButton("zoom In");
        zoomIn.addActionListener(this);
        zoomIn.setActionCommand("zoomIn");
        //-------------(2)----------------
        zoomOut = new JButton("zoom Out");
        zoomOut.addActionListener(this);
        zoomOut.setActionCommand("zoomOut");


//        // create jPanel
//        jPanel1 = new JPanel();
//        jPanel1.setLayout(new FlowLayout());


        // add button to JLabel
//        jPanel1.add(zoomIn);
//        jPanel1.add(zoomOut);


        // add jPanel to JFrame
//        add(jPanel1,BorderLayout.NORTH);


        //save
        this.setVisible(true);

        // request Focus to CanvasT (from Mouse Motion Listener , JText , ... )
        canvasT.requestFocusInWindow();

    }

    // ------------------ ActionListener ---------------------
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()){
            case "1" :
                System.out.println("1");
                canvasT.repaint();
                break;
            case "2" :
                System.out.println("2");
                canvasT.repaint();
                break;
            case "3" :
                System.out.println("3");
                canvasT.repaint();
                break;

        }

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
