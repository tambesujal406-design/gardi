package com.gardi.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/** Vertical station line, each section coloured by crowd level. The line draws itself station by station. */
public class HeatLineView extends View {
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint name = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint time = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float d;
    private String[] names = new String[0];
    private String[] times = new String[0];
    private int[] levels = new int[0];
    private float progress = 1f;

    public HeatLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        d = getResources().getDisplayMetrics().density;
        line.setStrokeWidth(5 * d);
        line.setStrokeCap(Paint.Cap.ROUND);
        name.setColor(0xFFFFFFFF);
        name.setTextSize(sp(16));
        time.setColor(0xFFCFC8A0);
        time.setTextSize(sp(13));
        time.setTextAlign(Paint.Align.RIGHT);
    }

    private float sp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, getResources().getDisplayMetrics());
    }

    public void setData(String[] names, String[] times, int[] levels) {
        this.names = names;
        this.times = times;
        this.levels = levels;
        requestLayout();
        ValueAnimator a = ValueAnimator.ofFloat(0f, 1f);
        a.setDuration(1400);
        a.addUpdateListener(v -> {
            progress = (float) v.getAnimatedValue();
            invalidate();
        });
        a.start();
    }

    @Override
    protected void onMeasure(int w, int h) {
        int height = (int) (names.length * 46 * d + 24 * d);
        setMeasuredDimension(resolveSize(0, w), resolveSize(height, h));
    }

    @Override
    protected void onDraw(Canvas c) {
        int n = names.length;
        if (n == 0) return;
        float x = 28 * d, row = 46 * d, top = 20 * d;
        float reveal = progress * (n - 1);
        for (int i = 0; i < n - 1; i++) {
            float frac = Math.max(0f, Math.min(1f, reveal - i));
            if (frac <= 0f) break;
            line.setColor(CrowdEngine.color(levels[i]));
            float y = top + i * row;
            c.drawLine(x, y, x, y + row * frac, line);
        }
        for (int i = 0; i < n; i++) {
            float y = top + i * row;
            dot.setStyle(Paint.Style.FILL);
            dot.setColor(0xFF111111);
            c.drawCircle(x, y, 9 * d, dot);
            dot.setStyle(Paint.Style.STROKE);
            dot.setStrokeWidth(3 * d);
            dot.setColor(0xFFF7C600);
            c.drawCircle(x, y, 9 * d, dot);
            c.drawText(names[i], x + 28 * d, y + 5 * d, name);
            c.drawText(times[i], getWidth() - 16 * d, y + 5 * d, time);
        }
    }
}
