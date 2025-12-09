package com;


import com.sun.opengl.util.FPSAnimator;

import javax.media.opengl.GLJPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameWindow extends JFrame {

    private CardLayout card;
    private JPanel container;

    private Menu menu;
    private StartMenu startMenu;
    private HowToPlay howToPlay;

    // GL parts (initialized when game starts)
    private GLJPanel GLJPanel;
    private GLCanvasProject lo;
    private FPSAnimator animator;

    public GameWindow() {
        setTitle("Brick Breaker");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        card = new CardLayout();
        container = new JPanel(card);

        // create panels
        menu = new Menu();
        startMenu = new StartMenu();
        howToPlay = new HowToPlay();

        container.add(menu, "menu");
        container.add(startMenu, "startMenu");
        container.add(howToPlay, "how");

        add(container, BorderLayout.CENTER);

        // menu actions
        menu.setStartAction(e -> card.show(container, "startMenu"));
        menu.setHowToPlayAction(e -> card.show(container, "how"));
        menu.setCloseAction(e -> exitGame());

        // startMenu actions
        startMenu.setOnePlayerAction(e -> startGame(1));
        startMenu.setTwoPlayersAction(e -> startGame(2));
        startMenu.setBackAction(e -> card.show(container, "menu"));

        // howToPlay back button (the HowToPlay class has a dummy back button)
        // we'll find it and wire it (simple approach)
        for (Component c : howToPlay.getComponents()) {
            if (c instanceof JPanel) {
                JPanel p = (JPanel) c;
                for (Component b : p.getComponents()) {
                    if (b instanceof JButton) {
                        ((JButton) b).addActionListener(ev -> card.show(container, "menu"));
                    }
                }
            }
        }

        // ensure we stop animator on close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanupGL();
            }
        });

        setVisible(true);
    }

    private void startGame(int players) {
        // if already started, cleanup first
        cleanupGL();

        // build GLCanvas and attach GLEventListener
        lo = new GLCanvasProject();
        lo.setPlayers(players); // important: set mode before init

        GLJPanel = new GLJPanel();

        GLJPanel.addGLEventListener(lo);

        // Key & mouse listeners
        GLJPanel.addKeyListener(lo);
        GLJPanel.addMouseListener(lo);
        GLJPanel.addMouseMotionListener(lo);

        // create a layered pane so we can overlay a small Pause button on top of the GLJPanel
        JLayeredPane layered = new JLayeredPane();
        JPanel gamePanel = new JPanel(new BorderLayout());
        gamePanel.add(layered, BorderLayout.CENTER);

        // create pause button (overlay)
        final JButton pauseButton = new JButton("Pause");
        pauseButton.setFocusable(false);
        pauseButton.setMargin(new Insets(4, 8, 4, 8));
        pauseButton.setOpaque(true);
        // semi-transparent background effect (may depend on LAF)
        pauseButton.setBackground(new Color(0, 0, 0, 120));
        pauseButton.setForeground(Color.WHITE);

        // initial bounds (will be updated on resize)
        GLJPanel.setBounds(0, 0, 800, 520);
        pauseButton.setBounds(10, 10, 90, 34);

        // add GLJPanel to layered pane (default layer)
        layered.add(GLJPanel, JLayeredPane.DEFAULT_LAYER);
        // add pause button on top layer
        layered.add(pauseButton, JLayeredPane.PALETTE_LAYER);

        // ensure the layered pane resizes children when its size changes
        layered.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                Dimension size = layered.getSize();
                // make GLJPanel fill the layered pane
                GLJPanel.setBounds(0, 0, size.width, size.height);
                // position pause button in top-left with small margin
                int bw = 90;
                int bh = 34;
                int margin = 10;
                pauseButton.setBounds(margin, margin, bw, bh);
            }
        });

        // action when pause pressed: stop animator, show options, then resume or go back
        pauseButton.addActionListener(e -> {
            // pause animator if running
            try {
                if (animator != null && animator.isAnimating()) animator.stop();
            } catch (Exception ignored) {}

            String[] options = {"Resume", "Back to Menu"};
            int choice = JOptionPane.showOptionDialog(GameWindow.this,
                    "Game Paused",
                    "Pause",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (choice == 0) { // Resume
                try {
                    if (animator != null && !animator.isAnimating()) animator.start();
                } catch (Exception ignored) {}
                GLJPanel.requestFocusInWindow();
            } else if (choice == 1) { // Back to Menu
                cleanupGL();
                card.show(container, "menu");
            } else { // closed dialog (treat as Resume)
                try {
                    if (animator != null && !animator.isAnimating()) animator.start();
                } catch (Exception ignored) {}
                GLJPanel.requestFocusInWindow();
            }
        });

        // add layered pane preferred size so layout managers have something
        layered.setPreferredSize(new Dimension(800, 520));

        // add game panel to container and show it
        container.add(gamePanel, "game");
        card.show(container, "game");

        // start animator
        animator = new FPSAnimator(GLJPanel, 60);
        animator.start();

        // request focus so key events go to GLJPanel
        GLJPanel.requestFocusInWindow();
    }

    private void cleanupGL() {
        try {
            if (animator != null) {
                animator.stop();
            }
        } catch (Exception ignored) {}
        animator = null;
        GLJPanel = null;
        lo = null;
    }


    private void exitGame() {
        cleanupGL();
        System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameWindow::new);
    }
}
