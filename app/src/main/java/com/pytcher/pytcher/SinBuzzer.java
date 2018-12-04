package com.pytcher.pytcher;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

public class SinBuzzer implements Runnable {
    private AudioTrack mAudioTrack;
    private double frequency;
    private int bufferSize;

    private double[] mSound;
    private short[] mBuffer;

    private boolean playSound = false;
    private boolean setUp;
    private boolean alive = true;

    SinBuzzer(int bufferSize) {
        this.bufferSize = bufferSize;
        setUpTrack();
        setUp = true;
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

    public boolean isSetUp() {
        return setUp;
    }

    public void updateFreq(double frequency) {
        this.frequency = frequency;
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
                        .setSampleRate(44100)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build();

        mSound = new double[bufferSize];
        mBuffer = new short[bufferSize];
    }

    private void writeToAudioSink(boolean canWrite) {
        if (canWrite) {
            for (int i = 0; i < mSound.length; i++) {
                mSound[i] = Math.sin((2.0 * Math.PI * frequency / 44100.0 * (double) i));
                mBuffer[i] = (short) (mSound[i] * Short.MAX_VALUE);
            }
            mAudioTrack.write(mBuffer, 0, mSound.length);
        }
    }
}
