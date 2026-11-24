package com.gardi.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/** Cartoon train mascot drawn with Canvas. Mood follows the crowd level and it bobs and blinks. */
public class MascotView extends View {
    private static final int INK = 0xFF111111, YELLOW = 0xFFF7C600, SWEAT = 0xFF7CC8F5;
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private int mood = CrowdEngine.LIGHT;
    private float phase = 0f;
    private ValueAnimator animator;

    public MascotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setMood(int level) {
        mood = level;
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1600);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(a -> {
            phase = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) animator.cancel();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int w, int h) {
        float d = getResources().getDisplayMetrics().density;
        setMeasuredDimension(resolveSize((int) (130 * d), w), resolveSize((int) (98 * d), h));
    }

    @Override
    protected void onDraw(Canvas c) {
        float s = Math.min(getWidth() / 120f, getHeight() / 90f);
        c.save();
        c.translate((getWidth() - 120 * s) / 2f, (getHeight() - 90 * s) / 2f);
        c.scale(s, s);

        // track
        stroke.setColor(INK);
        stroke.setStrokeWidth(3);
        c.drawLine(8, 84, 112, 84, stroke);

        // body (bobs up and down)
        c.save();
        c.translate(0, (float) (-2 - 2 * Math.sin(phase * 2 * Math.PI)));
        fill.setColor(INK);
        c.drawRoundRect(52, 1, 68, 10, 3, 3, fill);
        fill.setColor(0xFFFFFFFF);
        c.drawRoundRect(18, 8, 102, 70, 16, 16, fill);
        stroke.setStrokeWidth(4);
        c.drawRoundRect(18, 8, 102, 70, 16, 16, stroke);
        fill.setColor(INK);
        c.drawRoundRect(28, 17, 92, 44, 9, 9, fill);

        // eyes (blink every 4 seconds, squint when packed)
        boolean blink = SystemClock.uptimeMillis() % 4000 > 3850;
        float ry = blink ? 1f : (mood == CrowdEngine.PACKED ? 5f : 8f);
        fill.setColor(0xFFFFFFFF);
        c.drawOval(38, 31 - ry, 54, 31 + ry, fill);
        c.drawOval(66, 31 - ry, 82, 31 + ry, fill);
        if (!blink) {
            fill.setColor(INK);
            c.drawCircle(48, 32, 3.5f, fill);
            c.drawCircle(76, 32, 3.5f, fill);
        }

        // mouth
        stroke.setColor(INK);
        stroke.setStrokeWidth(3.5f);
        path.reset();
        if (mood == CrowdEngine.LIGHT) {
            path.moveTo(47, 55);
            path.quadTo(60, 65, 73, 55);
            c.drawPath(path, stroke);
        } else if (mood == CrowdEngine.BUSY) {
            path.moveTo(47, 57);
            path.quadTo(53.5f, 52, 60, 57);
            path.quadTo(66.5f, 62, 73, 57);
            c.drawPath(path, stroke);
        } else {
            fill.setColor(INK);
            c.drawOval(52, 52, 68, 63, fill);
        }

        // sweat drop when it is crowded
        if (mood != CrowdEngine.LIGHT) {
            path.reset();
            path.moveTo(98, 34);
            path.quadTo(103, 42, 98, 46);
            path.quadTo(93, 42, 98, 34);
            fill.setColor(SWEAT);
            c.drawPath(path, fill);
            stroke.setStrokeWidth(1.5f);
            c.drawPath(path, stroke);
        }

        // headlights
        fill.setColor(YELLOW);
        stroke.setStrokeWidth(2);
        c.drawCircle(31, 61, 4, fill);
        c.drawCircle(31, 61, 4, stroke);
        c.drawCircle(89, 61, 4, fill);
        c.drawCircle(89, 61, 4, stroke);
        c.restore();

        // wheels (black with a yellow ring so they show on dark backgrounds)
        fill.setColor(INK);
        c.drawCircle(36, 77, 7, fill);
        c.drawCircle(84, 77, 7, fill);
        stroke.setColor(YELLOW);
        stroke.setStrokeWidth(2f);
        c.drawCircle(36, 77, 6, stroke);
        c.drawCircle(84, 77, 6, stroke);
        fill.setColor(YELLOW);
        c.drawCircle(36, 77, 2.5f, fill);
        c.drawCircle(84, 77, 2.5f, fill);
        c.restore();
    }
}
