package com.gardi.app;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * SAMPLE timetable generated in code (Central line, Kalyan to Dadar and back).
 * Times are illustrative only. Replace generate() with a real timetable import later.
 */
public class Timetable {
    public static final String[] SLOW_STOPS = {"Kalyan", "Thakurli", "Dombivli", "Kopar", "Diva", "Mumbra",
            "Kalva", "Thane", "Mulund", "Nahur", "Bhandup", "Kanjurmarg", "Vikhroli", "Ghatkopar",
            "Vidyavihar", "Kurla", "Sion", "Matunga", "Dadar"};
    private static final int[] SLOW_MIN = {0, 5, 8, 11, 14, 18, 22, 26, 31, 34, 37, 40, 43, 47, 50, 53, 57, 61, 64};
    private static final String[] FAST_STOPS = {"Kalyan", "Dombivli", "Thane", "Mulund", "Ghatkopar", "Kurla", "Dadar"};
    private static final int[] FAST_MIN = {0, 8, 21, 27, 36, 42, 49};

    private static Timetable instance;
    private final List<Train> trains = new ArrayList<>();

    public static synchronized Timetable get() {
        if (instance == null) instance = new Timetable();
        return instance;
    }

    private Timetable() {
        generate(SLOW_STOPS, SLOW_MIN, false, 6);
        generate(FAST_STOPS, FAST_MIN, true, 9);
    }

    private void generate(String[] stops, int[] mins, boolean fast, int headway) {
        int n = stops.length;
        int total = mins[n - 1];
        for (int start = 300; start <= 1380; start += headway) {
            int[] upTimes = new int[n];
            int[] downTimes = new int[n];
            String[] reversed = new String[n];
            for (int i = 0; i < n; i++) {
                upTimes[i] = start + mins[i];
                downTimes[i] = start + (total - mins[n - 1 - i]);
                reversed[i] = stops[n - 1 - i];
            }
            trains.add(new Train(trains.size(), fast, true, stops, upTimes));
            trains.add(new Train(trains.size(), fast, false, reversed, downTimes));
        }
    }

    public Train train(int id) {
        return trains.get(id);
    }

    public static class Result {
        public final Train train;
        public final int from;
        public final int to;

        Result(Train train, int from, int to) {
            this.train = train;
            this.from = from;
            this.to = to;
        }

        public int depart() { return train.times[from]; }
        public int arrive() { return train.times[to]; }
    }

    public List<Result> search(String from, String to, int nowMinute, int max) {
        List<Result> out = new ArrayList<>();
        for (Train t : trains) {
            int a = t.indexOf(from);
            int b = t.indexOf(to);
            if (a < 0 || b < 0 || a >= b) continue;
            if (t.times[a] < nowMinute) continue;
            out.add(new Result(t, a, b));
        }
        Collections.sort(out, (x, y) -> Integer.compare(x.depart(), y.depart()));
        return out.size() > max ? new ArrayList<>(out.subList(0, max)) : out;
    }

    public static int nowMinute() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
    }

    public static String format(int minute) {
        int h = (minute / 60) % 24;
        int m = minute % 60;
        int h12 = (h % 12 == 0) ? 12 : h % 12;
        return h12 + ":" + (m < 10 ? "0" : "") + m + (h >= 12 ? " pm" : " am");
    }
}
