package com.pytcher.pytcher;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.v4.content.res.TypedArrayUtils;
import android.util.Log;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SinBuzzer implements Runnable {
    private AudioTrack mAudioTrack;
    private double frequency = 530;
    private int bufferSize;

    private double[] mSound;
    private short[] mBuffer;
    private List<Short> recordedBuffers;
    private Short[] recordedSound;

    private boolean playSound = false;
    private boolean setUp;
    private boolean alive = true;
    private boolean record = false;

    SinBuzzer(int bufferSize) {
        this.bufferSize = bufferSize;
        mSound = new double[bufferSize];
        mBuffer = new short[bufferSize];
        recordedBuffers = new LinkedList<>();
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
        setUpTrack();
    }

    public void stop() {
        mAudioTrack.pause();
        mAudioTrack.flush();
        mAudioTrack.release();
        primeAudioSink();
//        if (record) {
//            record = false;
//            recordedSound = (Short[]) recordedBuffers.toArray();
//        }
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

    public Short[] getRecordedSound() {
        return recordedSound;
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
                //Log.e("BUFFER WRITING", Arrays.toString(Arrays.copyOfRange(mBuffer, 0, 5)) + " " + Arrays.toString(Arrays.copyOfRange(mBuffer, mBuffer.length - 5, mBuffer.length)));
                //Log.e("FREQUENCY", Double.toString(frequency));
                mAudioTrack.write(mBuffer, 0, Math.min(mBuffer.length, (int) (44100 / frequency)));
//                if (record) {
//                    for (short sh : mBuffer) {
//                        recordedBuffers.add(sh);
//                    }
//                }
            } catch (NullPointerException nullE) {
                nullE.printStackTrace();
            }
        }
    }
}
