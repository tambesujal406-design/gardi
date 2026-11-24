package com.gardi.app;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private AutoCompleteTextView from, to;
    private MascotView mascot;
    private TextView bubble, empty;
    private TrainAdapter adapter;
    private TextView leaveBtn, nowBtn;
    private int chosen = -1; // minute of day picked by the user, or -1 for "now"
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            refresh();
            handler.postDelayed(this, 30000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        from = findViewById(R.id.from);
        to = findViewById(R.id.to);
        mascot = findViewById(R.id.mascot);
        bubble = findViewById(R.id.bubble);
        empty = findViewById(R.id.empty);
        findViewById(R.id.back).setOnClickListener(v -> finish());

        leaveBtn = findViewById(R.id.leaveBtn);
        nowBtn = findViewById(R.id.nowBtn);
        leaveBtn.setOnClickListener(v -> {
            int start = chosen >= 0 ? chosen : Timetable.nowMinute();
            new TimePickerDialog(this, (picker, hour, minute) -> {
                chosen = hour * 60 + minute;
                leaveBtn.setText("Leaving: " + Timetable.format(chosen));
                nowBtn.setVisibility(View.VISIBLE);
                refresh();
            }, start / 60, start % 60, false).show();
        });
        nowBtn.setOnClickListener(v -> {
            chosen = -1;
            leaveBtn.setText("Leaving: now");
            nowBtn.setVisibility(View.GONE);
            refresh();
        });

        ArrayAdapter<String> stations = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, Timetable.SLOW_STOPS);
        for (AutoCompleteTextView field : new AutoCompleteTextView[]{from, to}) {
            field.setAdapter(stations);
            field.setThreshold(0);
            field.setOnClickListener(v -> ((AutoCompleteTextView) v).showDropDown());
            field.setOnItemClickListener((parent, view, pos, id) -> refresh());
        }
        from.setText("Kalyan", false);
        to.setText("Dadar", false);

        findViewById(R.id.swap).setOnClickListener(v -> {
            String f = from.getText().toString();
            from.setText(to.getText().toString(), false);
            to.setText(f, false);
            refresh();
        });

        adapter = new TrainAdapter(r -> {
            Intent i = new Intent(this, TripActivity.class);
            i.putExtra("id", r.train.id);
            i.putExtra("from", r.from);
            i.putExtra("to", r.to);
            startActivity(i);
        });
        RecyclerView list = findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(tick);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(tick);
    }

    private void refresh() {
        String f = from.getText().toString().trim();
        String t = to.getText().toString().trim();
        int now = chosen >= 0 ? chosen : Timetable.nowMinute();
        List<Timetable.Result> results = f.equals(t)
                ? new ArrayList<>() : Timetable.get().search(f, t, now, 12);
        adapter.submit(results, now, chosen < 0);

        if (results.isEmpty()) {
            empty.setVisibility(View.VISIBLE);
            mascot.setMood(CrowdEngine.LIGHT);
            bubble.setText("Station chuno!");
            return;
        }
        empty.setVisibility(View.GONE);
        int level = CrowdEngine.level(CrowdEngine.scoreFor(results.get(0), CrowdEngine.isWeekend()));
        mascot.setMood(level);
        bubble.setText(level == CrowdEngine.PACKED ? "Bahut gardi hai!"
                : level == CrowdEngine.BUSY ? "Thodi gardi hai!" : "Mast jagah hai!");
    }
}
