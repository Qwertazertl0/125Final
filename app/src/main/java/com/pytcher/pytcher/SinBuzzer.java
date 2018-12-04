package com.pytcher.pytcher;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class SinBuzzer implements Runnable {
    private AudioTrack mAudioTrack;
    private double frequency;
    private int bufferSize;

    private double[] mSound;
    private short[] mBuffer;

    private boolean playSound = false;
    private boolean setUp = false;

    SinBuzzer(int bufferSize) {
        setUpTrack();
        this.bufferSize = bufferSize;
        setUp = true;
    }

    public void run() {
        while (true) {
            //Log.e("SinBuzzer:", Boolean.toString(mAudioTrack.getState() == 0));
            playSound();
        }
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
        int mBufferSize = AudioTrack.getMinBufferSize(44100,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                mBufferSize, AudioTrack.MODE_STREAM);

        // Sine wave
        mSound = new double[bufferSize];
        mBuffer = new short[bufferSize];
        //Log.e("Screw you", Integer.toString(mAudioTrack.getState()));
    }
    private void playSound() {
        // AudioTrack definition
        if (playSound) {
            for (int i = 0; i < mSound.length; i++) {
                mSound[i] = Math.sin((2.0 * Math.PI * frequency / 44100.0 * (double) i));
                mBuffer[i] = (short) (mSound[i] * Short.MAX_VALUE);
            }
            //Log.e("SinBuzzer, playSound: ", Boolean.toString(mAudioTrack == null));
            try {
                mAudioTrack.play();
                mAudioTrack.write(mBuffer, 0, mSound.length);
                mAudioTrack.stop();
                //mAudioTrack.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }


}
