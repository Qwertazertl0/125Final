package com.pytcher.pytcher;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

public class SinBuzzer implements Runnable {
    private AudioTrack mAudioTrack;
    private double frequency = 440;

    private double[] mSound;
    private short[] mBuffer;

    private boolean playSound = false;
    private boolean alive = true;
    private int sampleRate = 44100;

    private double volume = 1;

    SinBuzzer() {
        setUpTrack();
    }

    public void run() {
        while (alive) {
            writeToAudioSink(playSound);
        }
    }

    public void primeAudioSink() {
        //Prime the buffer to try and avoid underflowing the buffer
        mAudioTrack.flush();
        for (int i = 0; i < 5; i++) {
            writeToAudioSink(true);
        }
    }

    public void stop() {
        mAudioTrack.stop();
        mAudioTrack.flush();
        primeAudioSink();
    }

    public void play() {
        mAudioTrack.play();
    }

    public void updateFreq(double frequency) {
        this.frequency = frequency;
    }

    public void updateVolume(double volume) {
        this.volume = volume;
    }

    public void setPlaySound(boolean playSound) {
        this.playSound = playSound;
    }

    private void setUpTrack() {
        mAudioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build();
    }

    private void writeToAudioSink(boolean canWrite) {
        int bufferSize = (int) (sampleRate / frequency);
        mSound = new double[bufferSize];
        mBuffer = new short[bufferSize];
        if (canWrite) {
            double period = (double) sampleRate / frequency;
            for (int i = 0; i < mSound.length; i++) {
                mSound[i] = Math.sin((2.0 * Math.PI * (double) i) / period);
                mBuffer[i] = (short) (mSound[i] * Short.MAX_VALUE);
            }
            mAudioTrack.write(mBuffer, 0, mSound.length);
        }
    }
}
