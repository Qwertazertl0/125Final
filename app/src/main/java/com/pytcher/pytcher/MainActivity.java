package com.pytcher.pytcher;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private Button lock, mode;
    private FloatingActionButton play;
    private Context context;
    private TextView note, freq, warningText;
    private ImageView background;
    private boolean isPlay = false, scaleLock = false;
    private final int DEFAULT_BUTTON_COLOR = 0xFFD6D7D7;
    protected final static double FREQ_LOG_BASE = 1.059463094359;
    private final String[] frequencyList = {"A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#"};

    PitchConverter pitchConverter = new PitchConverter();
    SinBuzzer sinBuzzer = new SinBuzzer();
    Thread playThread = new Thread(sinBuzzer);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        play = findViewById(R.id.playNote);
        lock = findViewById(R.id.scaleToggle);
        context = this.getApplicationContext();
        note = findViewById(R.id.note);
        freq = findViewById(R.id.frequency);
        mode = findViewById(R.id.modeToggle);
        background = findViewById(R.id.noteBackground);
        warningText = findViewById(R.id.rotateWarning);

        playThread.start();

        play.setOnClickListener((v) -> {
            System.out.println("IsPlay: " + isPlay);
            if (!isPlay) {
                sinBuzzer.setPlaySound(true);
                sinBuzzer.play();
                play.setImageResource(R.drawable.ic_round_pause_24px);
            } else {
                sinBuzzer.stop();
                play.setImageResource(R.drawable.ic_round_play_arrow_24px);
            }
            isPlay = !isPlay;
        });

        lock.setOnClickListener((v) -> {
            pitchConverter.toggleScale();
            lock.setText(pitchConverter.getScaleName());
        });

        mode.setOnClickListener((v) -> {
            sinBuzzer.toggleMode();
            mode.setText(sinBuzzer.getModeName());
        });


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(context, "No accelerometer detected", Toast.LENGTH_SHORT).show();
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        float valueX = event.values[0];
        float valueY = event.values[1];
        float valueZ = event.values[2];
        setAccelValues(valueX, valueY, valueZ);
    }

    private void setAccelValues(float valueX, float valueY, float valueZ) {
        double angleA = Math.toDegrees(Math.atan2(valueX, valueZ));
        double angleB = Math.toDegrees(Math.atan2(valueY, valueZ));

        if (Math.abs(angleA) < 15 && Math.abs(angleB) < 15) {
            warningText.setVisibility(View.VISIBLE);
            background.getLayoutParams().height = 700;
        } else {
            warningText.setVisibility(View.INVISIBLE);
            background.getLayoutParams().height = 500;
        }

        double angle = Math.toDegrees(Math.atan2(valueY, valueX));

        double frequency = pitchConverter.getFrequency(angle);
        sinBuzzer.updateFreq(frequency);
        freq.setText(String.format("%.1f Hz", frequency));
        note.setText(freqToNote(frequency));
    }

    private String freqToNote(double freq) {
        int halfSteps = (int) Math.round(Math.log(freq / 440) / Math.log(FREQ_LOG_BASE));
        halfSteps %= 12;
        if (halfSteps < 0) {
            halfSteps += 12;
        }
        return frequencyList[halfSteps];
    }
}
