package com;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL; // يجب استيراد هذه المكتبة

public class StartMenu extends JPanel {

    // 1. تعريف متغير للصورة
    private Image backgroundImage;

    JButton onePlayerBtn;
    JButton twoPlayersBtn;
    JButton backBtn;

    ;

    public StartMenu() {

        try {
            String imagePath = "/com/Image/new2.jpeg";

            URL imageUrl = getClass().getResource(imagePath);

            if (imageUrl != null) {
                // نمرر الـ URL إلى ImageIcon لتحميل الصورة
                backgroundImage = new ImageIcon(imageUrl).getImage();

            } else {
                System.err.println(" فشل تحميل صورة StartMenu: لم يتم العثور على المسار: " + imagePath);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load background image.");
        }

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(16, 0, 16, 0);

        // إنشاء الأزرار
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

        // قم بإزالة setBackground(Color.BLACK); لتجنب تغطية الصورة
        // setBackground(Color.BLACK);
    }

    // 3. تجاوز دالة paintComponent لرسم الصورة
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // يجب استدعاء الدالة الأصلية أولاً

        if (backgroundImage != null) {
            // ارسم الصورة لملء اللوحة بالكامل
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    // 4. جعل الأزرار شفافة (Transparent) لتظهر الخلفية من ورائها
    private void styleButton(JButton btn) {
        btn.setFocusable(false);
        btn.setPreferredSize(new Dimension(240, 44));
        btn.setFont(new Font("Arial", Font.BOLD, 18));


        btn.setOpaque(false);
        btn.setContentAreaFilled(false);


        btn.setForeground(Color.WHITE);
    }

    private void styleSmall(JButton btn) {
        btn.setFocusable(false);
        btn.setPreferredSize(new Dimension(160, 34));
        btn.setFont(new Font("Arial", Font.PLAIN, 14));


        btn.setOpaque(false);
        btn.setContentAreaFilled(false);


        btn.setForeground(Color.WHITE);
    }

    // ... (Actions methods) ...
    public void setOnePlayerAction(ActionListener a) { onePlayerBtn.addActionListener(a); }
    public void setTwoPlayersAction(ActionListener a) { twoPlayersBtn.addActionListener(a); }
    public void setBackAction(ActionListener a) { backBtn.addActionListener(a); }
}
