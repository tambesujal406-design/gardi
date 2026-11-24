package com.gardi.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

/** Rings when the station alarm time arrives: alarm sound, vibration, and a notification that repeats until opened. */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL = "station_alarm";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String text = intent.getStringExtra("text");
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        long[] pattern = {0, 800, 400, 800, 400, 800, 400, 800};

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(CHANNEL, "Station alarm", NotificationManager.IMPORTANCE_HIGH);
            ch.setSound(sound, new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());
            ch.enableVibration(true);
            ch.setVibrationPattern(pattern);
            nm.createNotificationChannel(ch);
        }

        PendingIntent open = PendingIntent.getActivity(context, 3,
                new Intent(context, ModeActivity.class), PendingIntent.FLAG_IMMUTABLE);
        Notification n = new NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSound(sound, AudioManager.STREAM_ALARM)
                .setVibrate(pattern)
                .setContentIntent(open)
                .setAutoCancel(true)
                .build();
        n.flags |= Notification.FLAG_INSISTENT;

        AlarmHelper.clear(context);
        nm.notify(1, n);
    }
}
