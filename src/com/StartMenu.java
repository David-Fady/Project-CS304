
package Mygame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class StartMenu extends JPanel {

    JButton onePlayerBtn;
    JButton twoPlayersBtn;
    JButton backBtn;

    public StartMenu() {

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(16, 0, 16, 0);

        onePlayerBtn = new JButton("One Player");
        styleButton(onePlayerBtn);
        add(onePlayerBtn, gbc);

        gbc.gridy++;
        twoPlayersBtn = new JButton("Two Players");
        styleButton(twoPlayersBtn);
        add(twoPlayersBtn, gbc);

        gbc.gridy++;
        backBtn = new JButton("Back to Menu");
        styleSmall(backBtn);
        add(backBtn, gbc);

        setBackground(Color.BLACK);
    }

    private void styleButton(JButton btn) {
        btn.setFocusable(false);
        btn.setPreferredSize(new Dimension(240, 44));
        btn.setFont(new Font("Arial", Font.BOLD, 18));
    }

    private void styleSmall(JButton btn) {
        btn.setFocusable(false);
        btn.setPreferredSize(new Dimension(160, 34));
        btn.setFont(new Font("Arial", Font.PLAIN, 14));
    }

    public void setOnePlayerAction(ActionListener a) { onePlayerBtn.addActionListener(a); }
    public void setTwoPlayersAction(ActionListener a) { twoPlayersBtn.addActionListener(a); }
    public void setBackAction(ActionListener a) { backBtn.addActionListener(a); }
}

