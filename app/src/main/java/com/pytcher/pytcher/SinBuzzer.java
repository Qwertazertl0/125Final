package com.pytcher.pytcher;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class SinBuzzer implements Runnable {
    private AudioTrack mAudioTrack;
    private double frequency;
    private int bufferSize;

    private boolean playSound = false;
    public void run() {
        while (true) {
            playSound();
        }
    }

    SinBuzzer(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void updateFreq(double frequency) {
        this.frequency = frequency;
    }

    public void setPlaySound(boolean playSound) {
        this.playSound = playSound;
    }
    private void playSound() {
        // AudioTrack definition
        if (playSound) {
            int mBufferSize = AudioTrack.getMinBufferSize(44100,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    mBufferSize, AudioTrack.MODE_STREAM);

            // Sine wave
            double[] mSound = new double[bufferSize];
            short[] mBuffer = new short[bufferSize];
            for (int i = 0; i < mSound.length; i++) {
                mSound[i] = Math.sin((2.0 * Math.PI * frequency / 44100.0 * (double) i));
                mBuffer[i] = (short) (mSound[i] * Short.MAX_VALUE);
            }
            mAudioTrack.play();
            mAudioTrack.write(mBuffer, 0, mSound.length);
            mAudioTrack.stop();
            mAudioTrack.release();
        }
    }

}
