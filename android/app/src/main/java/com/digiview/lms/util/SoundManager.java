package com.digiview.lms.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import com.digiview.lms.R;

import android.media.MediaPlayer;

/**
 * Sound Manager for DigiView Android.
 * Plays the exact same audio assets used in the Desktop Swing version:
 * - mixkit_select_click_1109 (button and option selection)
 * - mixkit_single_key_type_2533 (typing/key effect)
 * - freesound_community_relax_32200 (relaxing ambient study music)
 */
public class SoundManager {

    private static SoundManager instance;
    private SoundPool soundPool;
    private MediaPlayer mediaPlayer;
    private int clickSoundId = -1;
    private int typeSoundId = -1;
    private boolean soundEnabled = true;
    private boolean musicPlaying = false;

    private SoundManager(Context context) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(audioAttributes)
                .build();

        try {
            clickSoundId = soundPool.load(context.getApplicationContext(), R.raw.mixkit_select_click_1109, 1);
            typeSoundId = soundPool.load(context.getApplicationContext(), R.raw.mixkit_single_key_type_2533, 1);
        } catch (Exception ignored) {}
    }

    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context);
        }
        return instance;
    }

    public void playClick() {
        if (!soundEnabled || soundPool == null || clickSoundId == -1) return;
        try {
            soundPool.play(clickSoundId, 0.75f, 0.75f, 1, 0, 1.0f);
        } catch (Exception ignored) {}
    }

    public void playType() {
        if (!soundEnabled || soundPool == null || typeSoundId == -1) return;
        try {
            soundPool.play(typeSoundId, 0.5f, 0.5f, 1, 0, 1.0f);
        } catch (Exception ignored) {}
    }

    public boolean toggleRelaxMusic(Context context) {
        if (mediaPlayer == null) {
            try {
                mediaPlayer = MediaPlayer.create(context.getApplicationContext(), R.raw.freesound_community_relax_32200);
                if (mediaPlayer != null) {
                    mediaPlayer.setLooping(true);
                    mediaPlayer.setVolume(0.4f, 0.4f);
                }
            } catch (Exception ignored) {}
        }

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                musicPlaying = false;
            } else {
                mediaPlayer.start();
                musicPlaying = true;
            }
        }
        return musicPlaying;
    }

    public boolean isMusicPlaying() {
        return musicPlaying && mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            musicPlaying = false;
        }
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }
}
