package com.digiview.lms.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import com.digiview.lms.R;

/**
 * Sound Manager for DigiView Android.
 * Plays the exact same audio assets used in the Desktop Swing version:
 * - mixkit_select_click_1109 (button and option selection)
 * - mixkit_single_key_type_2533 (typing/key effect)
 * - freesound_community_relax_32200 (relaxing ambient study music)
 */
public class SoundManager {

    private static final String TAG = "DigiViewSound";
    private static SoundManager instance;
    private SoundPool soundPool;
    private MediaPlayer mediaPlayer;
    private int clickSoundId = -1;
    private int typeSoundId = -1;
    private volatile boolean clickLoaded = false;
    private volatile boolean typeLoaded = false;
    private boolean soundEnabled = true;
    private boolean musicUserEnabled = true;
    private boolean musicPlaying = false;
    private final Context appContext;
    private long lastClickTime = 0;

    /**
     * Root symbol redirection for Android asset and resource paths.
     * Allows using "@" or other symbols to point to the root resource/asset folder:
     * e.g. "@/sounds/mixkit_select_click_1109" -> "sounds/mixkit_select_click_1109"
     */
    public static String redirectRootPath(String path) {
        if (path == null) return "";
        String p = path.trim();
        while (p.startsWith("@") || p.startsWith("~") || p.startsWith("^") || p.startsWith("$")) {
            p = p.substring(1).trim();
        }
        while (p.startsWith("/") || p.startsWith("\\")) {
            p = p.substring(1).trim();
        }
        return p;
    }

    private SoundManager(Context context) {
        this.appContext = context.getApplicationContext();
        initSoundPool();
    }

    private void initSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(8)
                .setAudioAttributes(audioAttributes)
                .build();

        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (status == 0) {
                if (sampleId == clickSoundId) clickLoaded = true;
                if (sampleId == typeSoundId) typeLoaded = true;
                Log.d(TAG, "Audio sample loaded successfully: " + sampleId);
            }
        });

        try {
            clickSoundId = soundPool.load(appContext, R.raw.mixkit_select_click_1109, 1);
            typeSoundId = soundPool.load(appContext, R.raw.mixkit_single_key_type_2533, 1);
        } catch (Exception e) {
            Log.e(TAG, "Error loading sounds", e);
        }
    }

    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context);
        }
        return instance;
    }

    public void playClick() {
        if (!soundEnabled) return;
        long now = System.currentTimeMillis();
        if (now - lastClickTime < 70) {
            return; // Throttle to prevent duplicate sound on rapid events
        }
        lastClickTime = now;

        // 1. Tactile haptic feedback
        triggerHaptic();

        // 2. SoundPool playback
        try {
            if (soundPool != null && clickSoundId != -1) {
                int streamId = soundPool.play(clickSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
                if (streamId != 0) return;
            }
            // 3. Instant MediaPlayer fallback if SoundPool stream wasn't ready
            MediaPlayer mp = MediaPlayer.create(appContext, R.raw.mixkit_select_click_1109);
            if (mp != null) {
                mp.setVolume(1.0f, 1.0f);
                mp.setOnCompletionListener(MediaPlayer::release);
                mp.start();
            }
        } catch (Exception ignored) {}
    }

    public void playType() {
        if (!soundEnabled) return;
        try {
            if (soundPool != null && typeSoundId != -1) {
                int streamId = soundPool.play(typeSoundId, 0.8f, 0.8f, 1, 0, 1.0f);
                if (streamId != 0) return;
            }
            MediaPlayer mp = MediaPlayer.create(appContext, R.raw.mixkit_single_key_type_2533);
            if (mp != null) {
                mp.setVolume(0.8f, 0.8f);
                mp.setOnCompletionListener(MediaPlayer::release);
                mp.start();
            }
        } catch (Exception ignored) {}
    }

    private void triggerHaptic() {
        try {
            Vibrator v = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null && v.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK));
                } else {
                    v.vibrate(12);
                }
            }
        } catch (Exception ignored) {}
    }

    public synchronized void startRelaxMusic(Context context) {
        if (!soundEnabled || !musicUserEnabled) return;
        if (mediaPlayer == null) {
            try {
                mediaPlayer = MediaPlayer.create(context.getApplicationContext(), R.raw.freesound_community_relax_32200);
                if (mediaPlayer != null) {
                    mediaPlayer.setLooping(true);
                    mediaPlayer.setVolume(0.65f, 0.65f);
                    mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                        try { mp.reset(); } catch (Exception ignored) {}
                        mediaPlayer = null;
                        musicPlaying = false;
                        return true;
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed creating relax music player", e);
            }
        }
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.start();
                musicPlaying = true;
                Log.d(TAG, "Relax music started automatically");
            } catch (Exception e) {
                Log.e(TAG, "Failed starting relax music", e);
            }
        }
    }

    public synchronized boolean toggleRelaxMusic(Context context) {
        if (!musicUserEnabled) {
            musicUserEnabled = true;
            startRelaxMusic(context);
            return isMusicPlaying();
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            musicPlaying = false;
            musicUserEnabled = false;
        } else {
            musicUserEnabled = true;
            startRelaxMusic(context);
        }
        return isMusicPlaying();
    }

    public synchronized boolean isMusicPlaying() {
        return musicUserEnabled && mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public synchronized void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            musicPlaying = false;
        }
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        if (!enabled) {
            pauseMusic();
        }
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }
}
