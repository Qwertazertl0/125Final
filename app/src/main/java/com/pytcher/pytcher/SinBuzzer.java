package com.pytcher.pytcher;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class SinBuzzer implements Runnable {
    private AudioTrack mAudioTrack;
    private Executor executor;
    private double frequency = 530;
    private int bufferSize;

    private short[] mBuffer;

    private boolean playSound = false;
    private boolean setUp;
    private boolean alive = true;
    private boolean record = false;

    SinBuzzer(int bufferSize) throws IOException {
        this.bufferSize = bufferSize;
        mBuffer = new short[bufferSize];
        executor = Executors.newSingleThreadExecutor();
        setUpTrack();
        setUp = true;
    }

    public void run() {
        while (alive) {
            writeToAudioSink(playSound);
        }
    }

    public void primeAudioSink() {
        mAudioTrack.flush();
        for (int i = 0; i < 5; i++) {
            writeToAudioSink(true);
        }
        setUpTrack();
    }

    public void stop() {
        mAudioTrack.pause();
        mAudioTrack.flush();
        mAudioTrack.release();
        primeAudioSink();

        record = false;
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

    public void setRecord(boolean rec) {
        this.record = rec;
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
                .setBufferSizeInBytes(AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT))
                .build();
    }

    private void writeToAudioSink(boolean canWrite) {
        if (canWrite) {
            for (int i = 0; i < mBuffer.length; i++) {
                mBuffer[i] = (short) (Math.sin((2.0 * Math.PI * frequency / 44100.0 * (double) i)) * Short.MAX_VALUE);
            }

            try {
                mAudioTrack.write(mBuffer, 0, Math.min(mBuffer.length, (int) (44100 / frequency)));
                if (record) {
                    executor.execute(() -> MainActivity.writeToCSV(mBuffer, Math.min(mBuffer.length, (int) (44100 / frequency))));
                }
            } catch (NullPointerException nullE) {
                nullE.printStackTrace();
            }
        }
    }

}
