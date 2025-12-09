
package Mygame;

import javax.swing.*;
import java.awt.*;

public class HowToPlay extends JPanel {

    public HowToPlay() {
        setLayout(new BorderLayout(10,10));
        JTextArea text = new JTextArea();
        text.setEditable(false);
        text.setFont(new Font("Arial", Font.PLAIN, 16));
        text.setLineWrap(true);
        text.setWrapStyleWord(true);

        String instr = "How to Play:\n\n"
                + "Single Player (One Player):\n"
                + "- Use the ARROW keys (Left / Right / Up / Down) to move the single paddle at the bottom.\n"
                + "- Goal: Keep the ball in play and break all bricks. Each brick gives points.\n"
                + "- Win: Destroy all bricks on the screen to clear the level and advance.\n"
                + "- Lose: If the ball falls below the paddle (falls off the bottom), you lose.\n\n"
                + "Two Players:\n"
                + "- Player 1: Use ARROW keys (Left / Right / Up / Down).\n"
                + "- Player 2: Use keys A (left), D (right), W (up), S (down).\n"
                + "- Both players control their own paddles. Cooperate to keep the ball alive and break bricks.\n\n"
                + "Scoring & Progress:\n"
                + "- Each brick you break increases your score.\n"
                + "- Clear all bricks to win the level; advancing levels increases difficulty/speed.\n\n"
                + "Tips:\n"
                + "- Aim the ball by hitting different parts of the paddle.\n"
                + "- Catching angles: hitting the edge of the paddle gives more horizontal velocity.\n";

        text.setText(instr);
        add(new JScrollPane(text), BorderLayout.CENTER);

        JButton back = new JButton("Back to Menu");
        back.setFocusable(false);
        back.addActionListener(e -> {
            // handled by GameWindow via CardLayout action binding
        });
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.CENTER));
        foot.add(back);
        foot.setOpaque(false);

        add(foot, BorderLayout.SOUTH);
        setBackground(Color.DARK_GRAY);
    }
}

