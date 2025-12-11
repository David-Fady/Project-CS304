package com;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class HowToPlay extends JPanel {

    public HowToPlay() {
        setLayout(new BorderLayout(10,10));

        JTextPane text = new JTextPane();
        text.setEditable(false);
        text.setFont(new Font("Arial", Font.BOLD, 18));
        text.setBackground(new Color(92, 204, 238));

        // Center Alignment
        StyledDocument doc = text.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        String instr =
                "\n\n"
                + "How to Play:\n\n"
                        + "Single Player (One Player):\n"
                        + "- Use the ARROW keys (Left / Right / Up / Down) to move the paddle.\n\n"
                        + "Two Players:\n"
                        + "- Player 1: ARROW keys.\n"
                        + "- Player 2: W A S D.\n\n"
                        + "Tips:\n"
                        + "- Hit different paddle spots to change the ball angle.\n\n"
                        + "- Goal: Keep the ball in play and break all bricks.\n\n";


        text.setText(instr);

        add(new JScrollPane(text), BorderLayout.CENTER);

        JButton back = new JButton("Back to Menu");
        back.setFocusable(false);
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.CENTER));
        foot.add(back);
        foot.setOpaque(false);

        add(foot, BorderLayout.SOUTH);
        setBackground(new Color(92, 204, 238));
    }
}
