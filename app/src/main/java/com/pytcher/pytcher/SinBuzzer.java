package com.pytcher.pytcher;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

public class SinBuzzer implements Runnable {
    private AudioTrack mAudioTrack;
    private double frequency = 440;

    private boolean playSound = false;
    private boolean alive = true;
    private int sampleRate = 44100;

    private double volume = 1;

    public static final int SIN_WAVE = 0;
    public static final int SQUARE_WAVE = 1;
    public static final int SAWTOOTH_WAVE = 2;
    private int[] modes = {SIN_WAVE, SQUARE_WAVE, SAWTOOTH_WAVE};
    private String[] modeNames = {"Sine", "Square", "Sawtooth"};
    private int mode = modes[0];


    SinBuzzer() {
        setUpTrack();
    }

    public void run() {
        while (alive) {
            writeToAudioSink();
        }
    }

    public void primeAudioSink() {
        //Prime the buffer to try and avoid underflowing the buffer
        mAudioTrack.flush();
        for (int i = 0; i < 5; i++) {
            writeToAudioSink();
        }
    }


    public void toggleMode() {
        mode = (mode + 1) % modes.length;
    }
    /**
     * Sets the different wave types.
     * @param mode the mode
     */
    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getModeName() {
        return modeNames[mode];
    }


    public void stop() {
        playSound = false;
        mAudioTrack.stop();
        mAudioTrack.flush();
        primeAudioSink();
    }

    public void play() {
        playSound = true;
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

    private void writeToAudioSink() {
        if (playSound) {
            int bufferSize = (int) (sampleRate / Math.abs(frequency));
            short[] mBuffer;
            switch (mode) {
                case SIN_WAVE: mBuffer = generateSinWave(bufferSize);
                break;
                case SQUARE_WAVE: mBuffer = generateSquareWave(bufferSize);
                break;
                case SAWTOOTH_WAVE: mBuffer = generateSawtoothWave(bufferSize);
                break;
                default: mBuffer = generateSinWave(bufferSize);
            }
            mAudioTrack.write(mBuffer, 0, mBuffer.length);
        }
    }

    private short[] generateSinWave(int bufferSize) {
        double[] mSound = new double[bufferSize];
        short[] mBuffer = new short[bufferSize];
        double period = (double) sampleRate / frequency;
        for (int i = 0; i < mSound.length; i++) {
            mSound[i] = Math.sin((2.0 * Math.PI * (double) i) / period);
            mBuffer[i] = (short) (mSound[i] * Short.MAX_VALUE);
        }
        return mBuffer;
    }

    private short[] generateSquareWave(int bufferSize) {
        double[] mSound = new double[bufferSize];
        short[] mBuffer = new short[bufferSize];
        double period = (double) sampleRate / frequency;
        for (int i = 0; i < mSound.length; i++) {
            mSound[i] = Math.sin((2.0 * Math.PI * (double) i) / period);
            if (mSound[i] > 0) {
                // Divide by twenty to make sounds sound equal
                mBuffer[i] = Short.MAX_VALUE / 20;
            } else {
                mBuffer[i] = -1 * Short.MAX_VALUE / 20;
            }
        }
        return mBuffer;
    }

    private short[] generateSawtoothWave(int bufferSize) {
        double[] mSound = new double[bufferSize];
        short[] mBuffer = new short[bufferSize];
        double period = (double) sampleRate / frequency;
        for (int i = 0; i < mSound.length; i++) {
            //  https://stackoverflow.com/questions/17604968/generate-sawtooth-tone-in-java-android
            mSound[i] = 2 * (i % (sampleRate / frequency)) / (sampleRate / frequency) - 1;
            mBuffer[i] = (short) (mSound[i] * Short.MAX_VALUE / 20);
        }
        return mBuffer;
    }
}
