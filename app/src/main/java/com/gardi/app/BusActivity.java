package com.gardi.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/** Placeholder until a current BEST route and stop dataset is available. */
public class BusActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus);
        findViewById(R.id.busBack).setOnClickListener(v -> finish());
        findViewById(R.id.busOpenTrain).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
