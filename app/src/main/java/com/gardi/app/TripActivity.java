package com.gardi.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class TripActivity extends AppCompatActivity {
    private static final int REQ_NOTIFICATIONS = 7;
    private TextView alarmInfo, alarmBtn;
    private String station, alarmTime, alarmTitle, alarmText, stopsLabel;
    private long trigger;
    private long requested; // alarm time waiting for the notification permission

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);

        Train t = Timetable.get().train(getIntent().getIntExtra("id", 0));
        int a = getIntent().getIntExtra("from", 0);
        int z = getIntent().getIntExtra("to", 0);
        int n = z - a + 1;
        boolean weekend = CrowdEngine.isWeekend();

        String[] names = new String[n];
        String[] times = new String[n];
        int[] levels = new int[n];
        for (int i = 0; i < n; i++) {
            names[i] = t.stops[a + i];
            times[i] = Timetable.format(t.times[a + i]);
            levels[i] = CrowdEngine.level(CrowdEngine.score(t.times[a + i], t.up, t.fast, names[i], weekend));
        }

        ((TextView) findViewById(R.id.tripTitle)).setText(t.stops[a] + " to " + t.stops[z]);
        ((TextView) findViewById(R.id.tripSub)).setText((t.fast ? "Fast" : "Slow") + " · " + n
                + " stations · crowd by section (estimate)");
        ((HeatLineView) findViewById(R.id.heat)).setData(names, times, levels);

        // Station alarm: rings 3 stops before the destination (or at the next stop on short trips)
        int k = Math.max(a + 1, z - 3);
        int before = z - k;
        station = t.stops[z];
        alarmTime = Timetable.format(t.times[k]);
        stopsLabel = before <= 0 ? "at" : (before == 1 ? "1 stop before" : before + " stops before");
        alarmTitle = "Wake up! " + station + " is coming";
        alarmText = "Arriving at " + station + " at " + Timetable.format(t.times[z]) + ". Estimated from the timetable.";

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.add(Calendar.MINUTE, t.times[k]);
        trigger = c.getTimeInMillis();

        alarmInfo = findViewById(R.id.alarmInfo);
        alarmBtn = findViewById(R.id.alarmBtn);
        alarmBtn.setOnClickListener(v -> onAlarmClick());
        // Press and hold: a test alarm that rings in 1 minute, to check sound and permissions at any time of day
        alarmBtn.setOnLongClickListener(v -> {
            startAlarm(System.currentTimeMillis() + 60000);
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAlarm();
    }

    private void refreshAlarm() {
        boolean on = trigger > 0 && AlarmHelper.pendingAt(this) == trigger;
        alarmInfo.setText(on ? "Alarm is on: rings at " + alarmTime
                : "Wake me " + stopsLabel + " " + station + " (rings at " + alarmTime + ")");
        alarmBtn.setText(on ? "Cancel alarm" : "Set alarm");
    }

    private void onAlarmClick() {
        if (trigger > 0 && AlarmHelper.pendingAt(this) == trigger) {
            AlarmHelper.cancel(this);
            refreshAlarm();
            return;
        }
        if (trigger <= System.currentTimeMillis() + 5000) {
            Toast.makeText(this, "This train is already too close to " + station + " to set an alarm.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        startAlarm(trigger);
    }

    private void startAlarm(long at) {
        requested = at;
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIFICATIONS);
            return;
        }
        setAlarm(at);
    }

    private void setAlarm(long at) {
        boolean test = at != trigger;
        try {
            AlarmHelper.set(this, at, alarmTitle, test ? "This is your 1-minute test alarm." : alarmText);
        } catch (SecurityException e) {
            // Newer Android versions ask for "Alarms & reminders" access before exact alarms
            Toast.makeText(this, "Allow Alarms & reminders for Gardi, then try again.", Toast.LENGTH_LONG).show();
            if (Build.VERSION.SDK_INT >= 31) {
                startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:" + getPackageName())));
            }
            return;
        } catch (RuntimeException e) {
            Toast.makeText(this, "Could not set the alarm: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        refreshAlarm();
        if (test) alarmInfo.setText("Test alarm is on: rings in 1 minute");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQ_NOTIFICATIONS) return;
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setAlarm(requested);
        } else {
            Toast.makeText(this, "Allow notifications so the alarm can wake you.", Toast.LENGTH_LONG).show();
        }
    }
}
