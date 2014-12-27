package eu.laprell.timetable.animation;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

import eu.laprell.timetable.utils.MetricsUtils;

public class RippleDrawable2 extends Drawable {

    private final int EASE_ANIM_DURATION = 200;
    private final int RIPPLE_ANIM_DURATION = 300;
    private final int MAX_RIPPLE_ALPHA = 180;

    private boolean hasRippleEffect = false;
    private int animDuration = EASE_ANIM_DURATION;

    private int mCircleAlpha = MAX_RIPPLE_ALPHA;
    private boolean isTouchReleased = false;
    private boolean isAnimatingFadeIn = false;

    private RadialGradient mRadial;

    private Matrix mMatrix;
    private Paint mPaint;

    private int mEffectColor;

    private float mCircleRadius;
    private float mTouchX, mTouchY;


    private Animation.AnimationListener animationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            isAnimatingFadeIn = true;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            isAnimatingFadeIn = false;
            if (isTouchReleased) fadeOutEffect();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    };

    public RippleDrawable2() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);

        mMatrix = new Matrix();

        mCircleRadius = MetricsUtils.convertDpToPixel(120);
    }

    private void updateRadial() {
        mCircleRadius = Math.max(getBounds().width(), getBounds().height()) * 0.6f;

        mRadial = new RadialGradient(mCircleRadius, mCircleRadius, mCircleRadius,
                new int[] {
                        getSemi(), getSemi(), Color.TRANSPARENT
                }, new float[] {
                0f, 0.8f, 1f
        }, Shader.TileMode.CLAMP);

        mRadial.setLocalMatrix(mMatrix);
        mPaint.setShader(mRadial);
    }

    private int getSemi() {
        return Color.argb(
                0x66,
                Color.red(mEffectColor),
                Color.green(mEffectColor),
                Color.blue(mEffectColor)
        );
    }

    public void setHasRippleEffect(boolean hasRippleEffect) {
        this.hasRippleEffect = hasRippleEffect;
        if (hasRippleEffect) animDuration = RIPPLE_ANIM_DURATION;
    }

    public void setAnimDuration(int animDuration) {
        this.animDuration = animDuration;
    }

    public void setEffectColor(int effectColor) {
        mEffectColor = effectColor;

        updateRadial();
    }

    public boolean onTouchEvent(final MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            isTouchReleased = true;
            if (!isAnimatingFadeIn) {
                fadeOutEffect();
            }

            return true;
        }
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            isTouchReleased = true;
            if (!isAnimatingFadeIn) {
                fadeOutEffect();
            }

            return true;
        } else if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {

            isTouchReleased = false;
            mTouchX = event.getX();
            mTouchY = event.getY();

            mCircleAlpha = MAX_RIPPLE_ALPHA;

            ValueGeneratorAnim valueGeneratorAnim = new ValueGeneratorAnim(new InterpolatedTimeCallback() {
                @Override
                public void onTimeUpdate(float interpolatedTime) {
                    mMatrix.setScale(interpolatedTime, interpolatedTime, mCircleRadius, mCircleRadius);
                    mRadial.setLocalMatrix(mMatrix);

                    invalidateSelf();
                }
            });
            valueGeneratorAnim.setInterpolator(new DecelerateInterpolator());
            valueGeneratorAnim.setDuration(animDuration);
            valueGeneratorAnim.setAnimationListener(animationListener);

            valueGeneratorAnim.start();

            return true;
        }

        return false;
    }

    private void fadeOutEffect() {
        ValueGeneratorAnim valueGeneratorAnim = new ValueGeneratorAnim(new InterpolatedTimeCallback() {
            @Override
            public void onTimeUpdate(float interpolatedTime) {
                mCircleAlpha = (int) (MAX_RIPPLE_ALPHA - (MAX_RIPPLE_ALPHA * interpolatedTime));
                invalidateSelf();
            }
        });
        valueGeneratorAnim.setDuration(animDuration);
        valueGeneratorAnim.start();
    }

    @Override
    public void draw(Canvas canvas) {
        if(hasRippleEffect) {
            float startX = mTouchX - mCircleRadius;
            float startY = mTouchY - mCircleRadius;

            mPaint.setAlpha(mCircleAlpha);

            int saveState = canvas.save();

            canvas.translate(startX, startY);
            canvas.drawRect(0, 0, mCircleRadius * 2, mCircleRadius * 2, mPaint);

            canvas.restoreToCount(saveState);
        }
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    class ValueGeneratorAnim extends Animation {

        private InterpolatedTimeCallback interpolatedTimeCallback;

        ValueGeneratorAnim(InterpolatedTimeCallback interpolatedTimeCallback) {
            this.interpolatedTimeCallback = interpolatedTimeCallback;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            this.interpolatedTimeCallback.onTimeUpdate(interpolatedTime);
        }
    }

    interface InterpolatedTimeCallback {
        public void onTimeUpdate(float interpolatedTime);
    }
}