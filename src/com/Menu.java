package com;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
// لم تعد بحاجة لـ java.io.IOException
// import java.io.IOException;
import java.net.URL; // نحتاجها لاستخدام URL

public class Menu extends JPanel {

    JButton startButton;
    JButton howToPlayButton;
    JButton closeButton;

    // 1. تعريف متغير للصورة
    private Image backgroundImage;
    // لم نعد بحاجة لـ absolutePath هنا
    // String absolutePath = "/com/Image/new2.jpeg";

    public Menu() {

        // 2. تحميل الصورة عند إنشاء الكائن
        try {
            // المسار النسبي الصحيح (نستخدمه مع getClass().getResource())
            String imagePath = "/com/Image/new2.jpeg";

            URL imageUrl = getClass().getResource(imagePath);

            if (imageUrl != null) {
                // نمرر الـ URL إلى ImageIcon لتحميل الصورة
                backgroundImage = new ImageIcon(imageUrl).getImage();

            } else {
                System.err.println(" فشل تحميل صورة Menu: لم يتم العثور على المسار: " + imagePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load background image.");
        }
        // ... (تم إزالة كود التحقق الإضافي checkUrl لأنه أصبح مدمجًا في try/catch) ...


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
    }

    // 4. تجاوز دالة paintComponent لرسم الصورة
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // هذا ضروري!
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    private void styleButton(JButton btn) {
        btn.setFocusable(false);
        btn.setPreferredSize(new Dimension(240, 44));
        btn.setFont(new Font("Arial", Font.BOLD, 18));


        btn.setOpaque(false); // الزر شفاف
        btn.setContentAreaFilled(false); // مساحة المحتوى شفافة

        // تعيين لون النص (Foreground) ليظهر على الصورة
        btn.setForeground(Color.WHITE); // يمكنك تغيير اللون حسب تفضيلك
    }

    public void setStartAction(ActionListener a) { startButton.addActionListener(a); }
    public void setHowToPlayAction(ActionListener a) { howToPlayButton.addActionListener(a); }
    public void setCloseAction(ActionListener a) { closeButton.addActionListener(a); }
}
