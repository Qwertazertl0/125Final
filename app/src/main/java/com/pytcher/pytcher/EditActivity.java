package com.pytcher.pytcher;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.SeekBar;

public class EditActivity extends AppCompatActivity {

    private ImageButton play;
    private SeekBar seekBar;
    private boolean isPlay = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit);

        play = findViewById(R.id.playButton);
        seekBar = findViewById(R.id.seekBar);

        play.setOnClickListener((v) -> {

        });
    }
}
