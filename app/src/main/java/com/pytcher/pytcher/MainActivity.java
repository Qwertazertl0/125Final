package com.pytcher.pytcher;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private Button set, record, lock;
    private Context context;
    private TextView note, freq;
    private boolean isSet = false, isRecord = false, scaleLock = false;
    private final int DEFAULT_BUTTON_COLOR = 0xFFD6D7D7;
    private final double FREQ_LOG_BASE = 1.059463094359;
    private final String[] frequencyList = {"A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#"};

    SinBuzzer sinBuzzer = new SinBuzzer();
    Thread playThread = new Thread(sinBuzzer);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        set = findViewById(R.id.setNote);
        record = findViewById(R.id.record);
        lock = findViewById(R.id.scaleToggle);
        context = this.getApplicationContext();
        note = findViewById(R.id.note);
        freq = findViewById(R.id.frequency);

        playThread.start();
        sinBuzzer.primeAudioSink();

        set.setOnClickListener((v) -> {
            if (!isRecord) {
                sinBuzzer.setPlaySound(!isSet);
                if (isSet) {
                    set.getBackground().setTint(DEFAULT_BUTTON_COLOR);
                    sinBuzzer.stop();
                } else {
                    set.getBackground().setTint(Color.RED);
                    sinBuzzer.play();
//                    Toast toast = Toast.makeText(context, "You are setting the note now", Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
//                    toast.show();
                }
                isSet = !isSet;
            } else {
                Toast.makeText(context, "Pause recording first!", Toast.LENGTH_SHORT).show();
            }
        });

        record.setOnClickListener((v) -> {
            if (!isSet) {
                sinBuzzer.setPlaySound(!isRecord);
                if (isRecord) {
                    record.getBackground().setTint(DEFAULT_BUTTON_COLOR);
                    sinBuzzer.stop();

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Would you like to save and edit your recording?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                } else {
                    record.getBackground().setTint(Color.RED);
//                    Toast.makeText(context, "You are recording now", Toast.LENGTH_SHORT).show();
//                    sinBuzzer.setRecord(true);
                    sinBuzzer.play();
                }
                isRecord = !isRecord;
            } else {
                Toast.makeText(context, "Finish setting the note first!", Toast.LENGTH_SHORT).show();
            }
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

    DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
        switch (which){
            case DialogInterface.BUTTON_POSITIVE:
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                //intent.putExtra("Recording", sinBuzzer.getRecordedSound());
                startActivity(intent);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                break;
        }
    };

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        float valueX = event.values[0];
        float valueY = event.values[1];
        float valueZ = event.values[2];
        setAccelValues(valueX, valueY, valueZ);
    }

    private void setAccelValues(float valueX, float valueY, float valueZ) {
        double angleA = Math.toDegrees(Math.atan(valueY / valueX));
        double newFreq = Math.abs(angleA) * 2.89 + 261;
        if (!scaleLock) {
            sinBuzzer.updateFreq(newFreq);
        } else {
            int halfSteps = (int) Math.round(Math.log(newFreq / 440) / Math.log(FREQ_LOG_BASE));
            halfSteps %= 12;
            sinBuzzer.updateFreq(440 * Math.pow(FREQ_LOG_BASE, halfSteps));
        }

        if (freq != null && note != null) {
            freq.setText(String.format("%6f Hz", newFreq));
            note.setText(freqToNote(newFreq));
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
