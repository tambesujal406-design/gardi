package com.gardi.app;

public class Train {
    public final int id;
    public final boolean fast;
    public final boolean up;      // true = towards Dadar / Mumbai
    public final String[] stops;
    public final int[] times;     // minute of day at each stop

    public Train(int id, boolean fast, boolean up, String[] stops, int[] times) {
        this.id = id;
        this.fast = fast;
        this.up = up;
        this.stops = stops;
        this.times = times;
    }

    public int indexOf(String station) {
        for (int i = 0; i < stops.length; i++) {
            if (stops[i].equals(station)) return i;
        }
        return -1;
    }
}
