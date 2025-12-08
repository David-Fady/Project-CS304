package com;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class SoundManager {

    // 1. المتغيرات الخاصة بملفات الصوت
    private Clip ballBounceClip;
    private Clip brickExplodeClip;
    private Clip levelWinClip;
    private Clip gameLostClip;

    // 2. مسارات ملفات الصوت الافتراضية
    private static final String BALL_BOUNCE_FILE = "sounds/bounce.wav";
    private static final String BRICK_EXPLODE_FILE = "sounds/explode.wav";
    private static final String LEVEL_WIN_FILE = "sounds/win.wav";
    private static final String GAME_LOST_FILE = "sounds/lost.wav";

    // 3. الباني (Constructor) - يستدعي loadSounds()
    public SoundManager() {
        // تحميل جميع الأصوات عند إنشاء الكائن
        loadSounds();
    }

    // 4. الدالة المفقودة التي تسببت في الخطأ
    private void loadSounds() {
        ballBounceClip = loadClip(BALL_BOUNCE_FILE);
        brickExplodeClip = loadClip(BRICK_EXPLODE_FILE);
        levelWinClip = loadClip(LEVEL_WIN_FILE);
        gameLostClip = loadClip(GAME_LOST_FILE);
    }

    // 5. دالة تحميل مقطع صوتي واحد
    private Clip loadClip(String filename) {
        try {
            URL soundUrl = SoundManager.class.getResource(filename);

            if (soundUrl == null) {
                File file = new File(filename);
                if (file.exists()) {
                    soundUrl = file.toURI().toURL();
                } else {
                    System.err.println("Sound file not found: " + filename);
                    return null;
                }
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundUrl);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            return clip;

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading sound file: " + filename);
            e.printStackTrace();
            return null;
        }
    }

    // 6. دوال تشغيل الأصوات
    public void playBallBounce() {
        playSound(ballBounceClip);
    }

    public void playBrickExplode() {
        playSound(brickExplodeClip);
    }

    public void playLevelWin() {
        playSound(levelWinClip);
    }

    public void playGameLost() {
        playSound(gameLostClip);
    }

    private void playSound(Clip clip) {
        if (clip != null) {
            clip.setFramePosition(0);
            clip.start();
        }
    }
}