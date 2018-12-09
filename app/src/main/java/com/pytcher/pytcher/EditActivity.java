package com.pytcher.pytcher;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class EditActivity extends AppCompatActivity {

    private AudioTrack recordingPlayer;
    private BufferedReader bufferedReader;
    private ImageButton play, pause;
    private Button back;
    private SeekBar seekBar;
    private Thread writePlayBack;
    private boolean isPlay = false;
    private int length;

    public EditActivity() throws FileNotFoundException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit);
        play = findViewById(R.id.playButton);
        pause = findViewById(R.id.pauseButton);
        pause.setVisibility(View.INVISIBLE);
        seekBar = findViewById(R.id.seekBar);
        back = findViewById(R.id.backButton);
        try {
            bufferedReader = new BufferedReader(new FileReader(this.getApplicationContext().getFilesDir().getPath() + "/recording.csv"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        recordingPlayer = new AudioTrack.Builder()
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
        
        writePlayBack = new Thread(new Runnable() {
            @Override
            public void run() {
                readFromRecordingFile();
            }
        });
        writePlayBack.start();

        play.setOnClickListener((v) -> {
            if (!isPlay) {
                recordingPlayer.play();
                play.setVisibility(View.INVISIBLE);
                pause.setVisibility(View.VISIBLE);
                isPlay = true;
            }
        });

        pause.setOnClickListener((v) -> {
            if (isPlay) {
                recordingPlayer.pause();
                play.setVisibility(View.VISIBLE);
                pause.setVisibility(View.INVISIBLE);
                isPlay = false;
            }
        });

        back.setOnClickListener((v) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
            builder.setMessage("Your recording will be lost. Are you sure?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        });
    }

    DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
        switch (which){
            case DialogInterface.BUTTON_POSITIVE:
                Intent intent = new Intent(EditActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                break;
        }
    };

    private void readFromRecordingFile() {
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Log.e("counter", "this happened");
                String[] pcmVals = line.split(",");
                short[] pcmValsShorts = new short[pcmVals.length - 1];
                for (int i = 1; i < pcmVals.length; i++) {
                    pcmValsShorts[i - 1] = Short.parseShort(pcmVals[i]);
                }
                recordingPlayer.write(pcmValsShorts, 0, pcmValsShorts.length);
                //Log.e("outputting from bufferedreader", Arrays.toString(pcmValsShorts));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
