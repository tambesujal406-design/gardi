package com.gardi.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/** Sets and cancels the one station alarm. Uses setAlarmClock, which fires even in battery saver and needs no special permission. */
public class AlarmHelper {
    private static final String PREFS = "gardi_alarm";

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static PendingIntent operation(Context c, String title, String text) {
        Intent i = new Intent(c, AlarmReceiver.class);
        i.putExtra("title", title);
        i.putExtra("text", text);
        return PendingIntent.getBroadcast(c, 1, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    public static void set(Context c, long triggerAt, String title, String text) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        PendingIntent show = PendingIntent.getActivity(c, 2, new Intent(c, ModeActivity.class), PendingIntent.FLAG_IMMUTABLE);
        am.setAlarmClock(new AlarmManager.AlarmClockInfo(triggerAt, show), operation(c, title, text));
        prefs(c).edit().putLong("at", triggerAt).apply();
    }

    public static void cancel(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        am.cancel(operation(c, "", ""));
        clear(c);
    }

    public static void clear(Context c) {
        prefs(c).edit().remove("at").apply();
    }

    /** Time of the alarm that is still waiting to ring, or 0 if there is none. */
    public static long pendingAt(Context c) {
        long at = prefs(c).getLong("at", 0);
        return at > System.currentTimeMillis() ? at : 0;
    }
}
