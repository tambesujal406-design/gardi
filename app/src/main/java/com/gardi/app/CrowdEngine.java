package com.gardi.app;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/** Heuristic crowd estimate (0-100). Not real data: time of day, direction, station type, weekday. */
public class CrowdEngine {
    public static final int LIGHT = 0, BUSY = 1, PACKED = 2;
    private static final Set<String> HUBS = new HashSet<>(Arrays.asList("Kalyan", "Thane", "Ghatkopar", "Kurla", "Dadar"));

    private static double bump(double x, double centre, double width) {
        double d = (x - centre) / width;
        return Math.exp(-d * d);
    }

    public static int score(int minute, boolean up, boolean fast, String station, boolean weekend) {
        double h = minute / 60.0;
        double morning = bump(h, 9.0, 1.4);
        double evening = bump(h, 18.8, 1.6);
        // Morning rush is towards Mumbai (up), evening rush is away from it (down)
        double load = up ? 0.95 * morning + 0.40 * evening : 0.40 * morning + 0.95 * evening;
        double s = 18 + 72 * load;
        if (fast) s += 6;
        if (HUBS.contains(station)) s += 8;
        if (weekend) s *= 0.65;
        return (int) Math.max(5, Math.min(100, Math.round(s)));
    }

    public static int scoreFor(Timetable.Result r, boolean weekend) {
        Train t = r.train;
        return score(r.depart(), t.up, t.fast, t.stops[r.from], weekend);
    }

    public static int level(int score) {
        return score >= 70 ? PACKED : (score >= 40 ? BUSY : LIGHT);
    }

    public static String label(int level) {
        return level == PACKED ? "Packed" : (level == BUSY ? "Busy" : "Light");
    }

    public static int color(int level) {
        return level == PACKED ? 0xFFF26B6B : (level == BUSY ? 0xFFF2B84B : 0xFF3DDBB0);
    }

    public static boolean isWeekend() {
        int d = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return d == Calendar.SATURDAY || d == Calendar.SUNDAY;
    }
}
