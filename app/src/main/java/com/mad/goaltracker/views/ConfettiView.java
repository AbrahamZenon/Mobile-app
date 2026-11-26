package com.mad.goaltracker.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Lightweight pure-Android Confetti particle burst overlay for celebration dialogs.
 */
public class ConfettiView extends View {

    private static final int[] DUO_CONFETTI_COLORS = {
            Color.parseColor("#58CC02"), // Feather Green
            Color.parseColor("#FFC800"), // Bee Gold
            Color.parseColor("#1CB0F6"), // Macaw Blue
            Color.parseColor("#FF9600"), // Fox Orange
            Color.parseColor("#CE82FF"), // Beetle Purple
            Color.parseColor("#FF4B4B")  // Cardinal Red
    };

    private static class Particle {
        float x, y;
        float vx, vy;
        float size;
        float rotation;
        float vRot;
        int color;
        boolean isCircle;
    }

    private final List<Particle> particles = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private ValueAnimator animator;
    private float progress = 0f;

    public ConfettiView(Context context) {
        super(context);
    }

    public ConfettiView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ConfettiView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void startBurst() {
        post(() -> {
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;

            particles.clear();
            int count = 48;
            float cx = w / 2f;
            float cy = h * 0.22f;

            for (int i = 0; i < count; i++) {
                Particle p = new Particle();
                p.x = cx + (random.nextFloat() - 0.5f) * (w * 0.35f);
                p.y = cy + (random.nextFloat() - 0.5f) * 30f;
                p.vx = (random.nextFloat() - 0.5f) * 14f;
                p.vy = -random.nextFloat() * 12f - 3f;
                p.size = 10f + random.nextFloat() * 12f;
                p.rotation = random.nextFloat() * 360f;
                p.vRot = (random.nextFloat() - 0.5f) * 16f;
                p.color = DUO_CONFETTI_COLORS[i % DUO_CONFETTI_COLORS.length];
                p.isCircle = (i % 3 == 0);
                particles.add(p);
            }

            if (animator != null) {
                animator.cancel();
            }
            animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(2200);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(anim -> {
                progress = (float) anim.getAnimatedValue();
                for (Particle p : particles) {
                    p.x += p.vx;
                    p.vy += 0.45f; // gentle gravity
                    p.y += p.vy;
                    p.rotation += p.vRot;
                }
                invalidate();
            });
            animator.start();
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (particles.isEmpty()) return;

        int alpha = (progress > 0.7f) ? (int) (255 * (1f - (progress - 0.7f) / 0.3f)) : 255;
        alpha = Math.max(0, Math.min(255, alpha));

        for (Particle p : particles) {
            paint.setColor(p.color);
            paint.setAlpha(alpha);
            canvas.save();
            canvas.translate(p.x, p.y);
            canvas.rotate(p.rotation);
            if (p.isCircle) {
                canvas.drawCircle(0, 0, p.size * 0.45f, paint);
            } else {
                canvas.drawRoundRect(-p.size * 0.5f, -p.size * 0.28f, p.size * 0.5f, p.size * 0.28f, 3f, 3f, paint);
            }
            canvas.restore();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) {
            animator.cancel();
        }
    }
}
