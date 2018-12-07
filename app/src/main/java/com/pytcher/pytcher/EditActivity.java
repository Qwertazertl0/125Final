package com.pytcher.pytcher;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.SeekBar;

public class EditActivity extends AppCompatActivity {

    private AudioTrack recordingPlayer;
    private ImageButton play;
    private SeekBar seekBar;
    private boolean isPlay = false;

    private short[] recording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Bundle extras = getIntent().getExtras();
//        recording = extras.getShortArray("Recording");

        setContentView(R.layout.activity_edit);
        play = findViewById(R.id.playButton);
        seekBar = findViewById(R.id.seekBar);

        recordingPlayer = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setSampleRate(44100)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build();

        play.setOnClickListener((v) -> {
            //recordingPlayer.play();
        });
    }
}
