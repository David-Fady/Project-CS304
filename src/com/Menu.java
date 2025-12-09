
package com;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class Menu extends JPanel {

    JButton startButton;
    JButton howToPlayButton;
    JButton closeButton;

    public Menu() {

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 0, 10, 0);

        // Start Button
        startButton = new JButton("Start");
        styleButton(startButton);
        add(startButton, gbc);

        // How to Play Button
        gbc.gridy++;
        howToPlayButton = new JButton("How to Play");
        styleButton(howToPlayButton);
        add(howToPlayButton, gbc);

        // Close Button
        gbc.gridy++;
        closeButton = new JButton("Close");
        styleButton(closeButton);
        add(closeButton, gbc);

        setBackground(Color.BLACK);
    }

    private void styleButton(JButton btn) {
        btn.setFocusable(false);
        btn.setPreferredSize(new Dimension(240, 44));
        btn.setFont(new Font("Arial", Font.BOLD, 18));
    }

    public void setStartAction(ActionListener a) { startButton.addActionListener(a); }
    public void setHowToPlayAction(ActionListener a) { howToPlayButton.addActionListener(a); }
    public void setCloseAction(ActionListener a) { closeButton.addActionListener(a); }
}

