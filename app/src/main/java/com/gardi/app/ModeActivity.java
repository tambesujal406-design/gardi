package com.gardi.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;

/** Choose Train or Bus. The tiles slide up when the screen opens. */
public class ModeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode);
        float d = getResources().getDisplayMetrics().density;

        View train = findViewById(R.id.tileTrain);
        View bus = findViewById(R.id.tileBus);
        View[] tiles = {train, bus};
        for (int i = 0; i < tiles.length; i++) {
            tiles[i].setTranslationY(80 * d);
            tiles[i].setAlpha(0f);
            tiles[i].animate().translationY(0f).alpha(1f).setStartDelay(150 + i * 120L).setDuration(500)
                    .setInterpolator(new OvershootInterpolator(1f)).start();
        }
        train.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        bus.setOnClickListener(v -> startActivity(new Intent(this, BusActivity.class)));
    }
}
