package com.gardi.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/** Dashed ground line. Changing the phase scrolls the dashes, so the train looks like it is moving. */
public class TrackView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float d;

    public TrackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        d = getResources().getDisplayMetrics().density;
        p.setColor(0xFF111111);
        p.setStrokeWidth(5 * d);
        p.setStyle(Paint.Style.STROKE);
        p.setPathEffect(new DashPathEffect(new float[]{20 * d, 14 * d}, 0));
    }

    public float cycle() {
        return 34 * d;
    }

    public void setPhase(float phase) {
        p.setPathEffect(new DashPathEffect(new float[]{20 * d, 14 * d}, phase));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas c) {
        float y = getHeight() / 2f;
        c.drawLine(0, y, getWidth(), y, p);
    }
}
