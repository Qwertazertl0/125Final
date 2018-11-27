package com.pytcher.pytcher;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button set, record;
    private Context context;
    private TextView note, freq;
    private boolean isSet = false, isRecord = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        set = (Button) findViewById(R.id.setNote);
        record = (Button) findViewById(R.id.record);
        context = this.getApplicationContext();
        note = (TextView) findViewById(R.id.note);
        freq = (TextView) findViewById(R.id.frequency);

        set.setOnClickListener((v) -> {
            isSet = !isSet;
            Toast.makeText(context, "You are setting the note", Toast.LENGTH_SHORT).show();
        });

        record.setOnClickListener((v) -> {
            if (isRecord) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Would you like to save and edit your recording?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
            isRecord = !isRecord;
            Toast.makeText(context, "You are recording now", Toast.LENGTH_SHORT).show();
        });
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
}
