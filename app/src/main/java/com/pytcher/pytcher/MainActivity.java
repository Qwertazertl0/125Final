package com.pytcher.pytcher;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView textX, textY, textZ;

    private Button set, record;
    private Context context;
    private TextView note, freq;
    private boolean isSet = false, isRecord = false;
    private final int DEFAULT_BUTTON_COLOR = 0xFFD6D7D7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        set = findViewById(R.id.setNote);
        record = findViewById(R.id.record);
        context = this.getApplicationContext();
        note = findViewById(R.id.note);
        freq = findViewById(R.id.frequency);


        set.setOnClickListener((v) -> {
            if (!isRecord) {
                if (isSet) {
                    set.getBackground().setTint(DEFAULT_BUTTON_COLOR);
                } else {
                    set.getBackground().setTint(Color.RED);
                    Toast toast = Toast.makeText(context, "You are setting the note now", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    toast.show();
                }
                isSet = !isSet;
            } else {
                Toast.makeText(context, "Pause recording first!", Toast.LENGTH_SHORT).show();
            }
        });

        record.setOnClickListener((v) -> {
            if (!isSet) {
                if (isRecord) {
                    record.getBackground().setTint(DEFAULT_BUTTON_COLOR);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Would you like to save and edit your recording?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                } else {
                    record.getBackground().setTint(Color.RED);
                    Toast.makeText(context, "You are recording now", Toast.LENGTH_SHORT).show();
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
        textX.setText(Float.toString(valueX));
        textY.setText(Float.toString(valueY));
        textZ.setText(Double.toString(angleA)); // Angle of the device, with 0 being horizontal
    }


}
