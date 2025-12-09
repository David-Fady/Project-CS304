
package Mygame;

import com.sun.opengl.util.FPSAnimator;
import javax.media.opengl.GLCanvas;
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
    private GLCanvas canvas;
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
        // iterate components to find the back button in howToPlay's south panel:
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

        canvas = new GLCanvas();
        canvas.addGLEventListener(lo);

        // Key & mouse listeners
        canvas.addKeyListener(lo);
        canvas.addMouseListener(lo);
        canvas.addMouseMotionListener(lo);

        // add canvas to a new panel and show it
        JPanel gamePanel = new JPanel(new BorderLayout());
        gamePanel.add(canvas, BorderLayout.CENTER);

        // Add a small top bar with Back to Menu button
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton back = new JButton("Back to Menu");
        back.setFocusable(false);
        back.addActionListener(e -> {
            cleanupGL();
            card.show(container, "menu");
        });
        top.add(back);
        gamePanel.add(top, BorderLayout.NORTH);

        container.add(gamePanel, "game");
        card.show(container, "game");

        // start animator
        animator = new FPSAnimator(canvas, 60);
        animator.start();

        canvas.requestFocusInWindow();
    }

    private void cleanupGL() {
        try {
            if (animator != null) {
                animator.stop();
            }
        } catch (Exception ignored) {}
        animator = null;
        canvas = null;
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

