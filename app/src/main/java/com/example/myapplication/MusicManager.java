package com.example.myapplication;

import android.content.Context;
import android.media.MediaPlayer;

public class MusicManager {

    private static MusicManager instance;
    private MediaPlayer backgroundMusicPlayer;

    private MusicManager() {
    }

    public static MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }

    public void initialize(Context context) {
        if (backgroundMusicPlayer == null) {
            backgroundMusicPlayer = MediaPlayer.create(context.getApplicationContext(), R.raw.background);
            backgroundMusicPlayer.setLooping(true);
            backgroundMusicPlayer.setVolume(1.0f, 1.0f);
        }
    }

    public void startMusic() {
        if (backgroundMusicPlayer != null && !backgroundMusicPlayer.isPlaying()) {
            backgroundMusicPlayer.start();
        }
    }

    public void pauseMusic() {
        if (backgroundMusicPlayer != null && backgroundMusicPlayer.isPlaying()) {
            backgroundMusicPlayer.pause();
        }
    }

    public void release() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.release();
            backgroundMusicPlayer = null;
        }
        instance = null;
    }
}
