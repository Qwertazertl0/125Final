package com.pytcher.pytcher;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private Button play, lock;
    private Context context;
    private TextView note, freq;
    private boolean isPlay = false, scaleLock = false;
    private final int DEFAULT_BUTTON_COLOR = 0xFFD6D7D7;
    private final double FREQ_LOG_BASE = 1.059463094359;
    private final String[] frequencyList = {"A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#"};

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

        playThread.start();
        sinBuzzer.primeAudioSink();

        play.setOnClickListener((v) -> {
            if (isPlay) {
                sinBuzzer.setPlaySound(true);
                play.getBackground().setTint(Color.RED);
                sinBuzzer.play();
            } else {
                play.getBackground().setTint(DEFAULT_BUTTON_COLOR);
                sinBuzzer.stop();
            }
            isPlay = !isPlay;
        });

        lock.setOnClickListener((v) -> {
            scaleLock = !scaleLock;
            if (scaleLock) {
                lock.getBackground().setTint(Color.RED);
            } else {
                lock.getBackground().setTint(DEFAULT_BUTTON_COLOR);
            }
        });


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
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
        double angleA = Math.toDegrees(Math.atan2(valueY, valueX));
        double newFreq = Math.abs(angleA) * 1.44 + 261;
        System.out.println(angleA);
        if (!scaleLock) {
            sinBuzzer.updateFreq(newFreq);
            if (freq != null && note != null) {
                freq.setText(String.format("%.1f Hz", newFreq));
                note.setText(freqToNote(newFreq));
            }
        } else {
            int halfSteps = (int) Math.round(Math.log(newFreq / 440) / Math.log(FREQ_LOG_BASE));
            halfSteps %= 12;
            double noteFreq = 440 * Math.pow(FREQ_LOG_BASE, halfSteps);
            sinBuzzer.updateFreq(noteFreq);
            if (freq != null && note != null) {
                freq.setText(String.format("%.1f Hz", noteFreq));
                note.setText(freqToNote(newFreq));
            }
        }
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
