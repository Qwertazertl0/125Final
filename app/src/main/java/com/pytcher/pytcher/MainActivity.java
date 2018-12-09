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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView textX, textY, textZ;
    private Button set, record;
    private Context context;
    private TextView note, freq;
    static private BufferedWriter bufferedWriter;
    private boolean isSet = false, isRecord = false;
    private final int DEFAULT_BUTTON_COLOR = 0xFFD6D7D7;
    private final double FREQ_LOG_BASE = 1.059463094359;
    private final String[] frequencyList = {"A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#"};
    private File recording;


    SinBuzzer sinBuzzer = new SinBuzzer(4410);
    Thread playThread = new Thread(sinBuzzer);

    public MainActivity() throws IOException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        set = findViewById(R.id.setNote);
        record = findViewById(R.id.record);
        context = this.getApplicationContext();
        note = findViewById(R.id.note);
        freq = findViewById(R.id.frequency);
        recording = new File(context.getFilesDir(), "recording.csv");

        playThread.start();
        sinBuzzer.primeAudioSink();
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(recording));
        } catch (IOException e) {
            e.printStackTrace();
        }

        set.setOnClickListener((v) -> {
            if (!isRecord) {
                sinBuzzer.setPlaySound(!isSet);
                if (isSet) {
                    set.getBackground().setTint(DEFAULT_BUTTON_COLOR);
                    sinBuzzer.stop();
                } else {
                    set.getBackground().setTint(Color.RED);
                    sinBuzzer.play();
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
                    builder.setMessage("Would you like to playback your recording?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                } else {
                    record.getBackground().setTint(Color.RED);
                    sinBuzzer.setRecord(true);
                    sinBuzzer.play();
                }
                isRecord = !isRecord;
            } else {
                Toast.makeText(context, "Finish setting the note first!", Toast.LENGTH_SHORT).show();
            }
        });

        initializeViews();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            Toast.makeText(context, "No accelerometer dectected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        recording = new File(context.getFilesDir(), "recording.csv");
    }

    DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
        switch (which){
            case DialogInterface.BUTTON_POSITIVE:
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                startActivity(intent);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                break;
        }
    };

    private void initializeViews() {
        textX = findViewById(R.id.accelerometerX);
        textY = findViewById(R.id.accelerometerY);
        textZ = findViewById(R.id.angle);
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
        double angleA = Math.toDegrees(Math.atan(valueY / valueX));
        double newFreq = Math.abs(angleA) * 2.89 + 261;
        sinBuzzer.updateFreq(newFreq);
        if (freq != null && note != null) {
            freq.setText(String.format("%6f Hz", newFreq));
            note.setText(freqToNote(newFreq));
        }
        textX.setText(Float.toString(valueX));
        textY.setText(Float.toString(valueY));
        textZ.setText(Double.toString(angleA)); // Angle of the device, with 0 being horizontal
    }

    private String freqToNote(double freq) {
        int halfSteps = (int) Math.round(Math.log(freq / 440) / Math.log(FREQ_LOG_BASE));
        halfSteps %= 12;
        if (halfSteps < 0) {
            halfSteps += 12;
        }
        return frequencyList[halfSteps];
    }

    public static void writeToCSV(short[] buffer, int length) {
        int counter = 0;
        boolean newline = false;
        try {
            for (int i = 0; i < Math.min(buffer.length, length + 1); i++) {
                bufferedWriter.append(",");
                bufferedWriter.append(Short.toString(buffer[i]));
                counter++;
                if (counter > 100) {
                    bufferedWriter.newLine();
                    counter -= 100;
                    newline = true;
                }
            }
            if (!newline) {
                bufferedWriter.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
