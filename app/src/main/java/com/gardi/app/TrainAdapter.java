package com.gardi.app;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TrainAdapter extends RecyclerView.Adapter<TrainAdapter.VH> {
    public interface OnTrainClick {
        void onClick(Timetable.Result result);
    }

    private final OnTrainClick listener;
    private List<Timetable.Result> items = new ArrayList<>();
    private int now;
    private boolean weekend;
    private boolean live = true;

    public TrainAdapter(OnTrainClick listener) {
        this.listener = listener;
    }

    public void submit(List<Timetable.Result> results, int nowMinute, boolean live) {
        items = results;
        now = nowMinute;
        this.live = live;
        weekend = CrowdEngine.isWeekend();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_train, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Timetable.Result r = items.get(position);
        Train t = r.train;
        int score = CrowdEngine.scoreFor(r, weekend);
        int level = CrowdEngine.level(score);
        int color = CrowdEngine.color(level);
        int wait = r.depart() - now;

        h.time.setText(Timetable.format(r.depart()));
        h.label.setText(CrowdEngine.label(level));
        h.label.setTextColor(color);
        int stops = r.to - r.from;
        h.sub.setText((t.fast ? "Fast" : "Slow") + " · " + stops + (stops == 1 ? " stop" : " stops") + " · arrives " + Timetable.format(r.arrive()));
        h.eta.setText(!live ? "" : (wait <= 0 ? "now" : "in " + wait + " min"));
        h.meter.setProgress(score);
        h.meter.setProgressTintList(ColorStateList.valueOf(color));
        h.itemView.setOnClickListener(v -> listener.onClick(r));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView time, label, sub, eta;
        final ProgressBar meter;

        VH(View v) {
            super(v);
            time = v.findViewById(R.id.time);
            label = v.findViewById(R.id.label);
            sub = v.findViewById(R.id.sub);
            eta = v.findViewById(R.id.eta);
            meter = v.findViewById(R.id.meter);
        }
    }
}
