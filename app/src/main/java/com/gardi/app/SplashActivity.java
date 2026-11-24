package com.gardi.app;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/** Intro: the train rolls in, the letters flip in like a station board, then it moves on by itself. */
public class SplashActivity extends AppCompatActivity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable next = this::goNext;
    private boolean done;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        float d = getResources().getDisplayMetrics().density;

        View root = findViewById(R.id.splashRoot);
        MascotView mascot = findViewById(R.id.mascot);
        TrackView track = findViewById(R.id.track);
        LinearLayout letters = findViewById(R.id.letters);
        TextView tag = findViewById(R.id.tag);

        // 1. Train rolls in from the left and settles with a small bounce
        mascot.setTranslationX(-getResources().getDisplayMetrics().widthPixels);
        mascot.animate().translationX(0f).setDuration(1500)
                .setInterpolator(new OvershootInterpolator(0.6f)).start();

        // The dashed track scrolls while the train arrives
        ValueAnimator scroll = ValueAnimator.ofFloat(0f, track.cycle() * 5);
        scroll.setDuration(1500);
        scroll.setInterpolator(new DecelerateInterpolator());
        scroll.addUpdateListener(a -> track.setPhase((float) a.getAnimatedValue()));
        scroll.start();

        // 2. Letters flip in one by one
        String word = "GARDI";
        for (int i = 0; i < word.length(); i++) {
            TextView t = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) (44 * d), (int) (62 * d));
            lp.setMargins((int) (3 * d), 0, (int) (3 * d), 0);
            t.setLayoutParams(lp);
            t.setBackgroundResource(R.drawable.bg_tile);
            t.setGravity(Gravity.CENTER);
            t.setText(String.valueOf(word.charAt(i)));
            t.setTextColor(0xFFF7C600);
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 38);
            t.setTypeface(Typeface.DEFAULT_BOLD);
            t.setCameraDistance(8000 * d);
            t.setRotationX(90f);
            t.setAlpha(0f);
            letters.addView(t);
            t.animate().rotationX(0f).alpha(1f).setStartDelay(1300 + i * 110L).setDuration(550)
                    .setInterpolator(new OvershootInterpolator(1.2f)).start();
        }

        // 3. Tagline fades in, then we move on (or the user taps to skip)
        tag.animate().alpha(1f).setStartDelay(2500).setDuration(500).start();
        root.setOnClickListener(v -> goNext());
        handler.postDelayed(next, 3800);
    }

    private void goNext() {
        if (done) return;
        done = true;
        startActivity(new Intent(this, ModeActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(next);
        super.onDestroy();
    }
}
