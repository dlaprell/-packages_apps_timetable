package eu.laprell.timetable.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import eu.laprell.timetable.utils.MetricsUtils;

/**
 * Created by david on 22.11.14.
 */
public class RippleDrawable extends Drawable {

    private static final int ANIM_TIME = 600;

    private static final DecelerateInterpolator sInterpolator = new DecelerateInterpolator(2);

    private RadialGradient mRadial;
    private ObjectAnimator mObjectAnimator;

    private Matrix mMatrix;
    private Paint mPaint;

    private float mAnimationStep;
    private float mCircleRadius;
    private float mTouchX, mTouchY;

    private boolean mInAnimation;
    private boolean mTouched;

    private Animator.AnimatorListener mListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            mInAnimation = false;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            mInAnimation = true;
        }
    };

    public RippleDrawable() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);

        mMatrix = new Matrix();

        mCircleRadius = MetricsUtils.convertDpToPixel(120);
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);

        updateRadial();
    }

    private void updateRadial() {
        mCircleRadius = Math.max(getBounds().width(), getBounds().height()) * 0.9f;

        mRadial = new RadialGradient(mCircleRadius, mCircleRadius, mCircleRadius,
                new int[] {
                        getSemi(), getSemi(), Color.TRANSPARENT
                }, new float[] {
                        0f, 0.8f, 1f
        }, Shader.TileMode.CLAMP);

        mRadial.setLocalMatrix(mMatrix);
        mPaint.setShader(mRadial);
    }

    private static final float DP_1 = MetricsUtils.convertPixelsToDp(1);
    public boolean onTouchEvent(MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);

        if(action == MotionEvent.ACTION_DOWN) {
            mTouchX = ev.getX();
            mTouchY = ev.getY();

            mObjectAnimator = ObjectAnimator.ofFloat(this, "animationStep", 0.01f, 1f);
            mObjectAnimator.setDuration((long) ((mCircleRadius / DP_1) * 1.3f));
            mObjectAnimator.setInterpolator(new DecelerateInterpolator());
            mObjectAnimator.addListener(mListener);
            mObjectAnimator.start();

            mTouched = true;

            return true;
        } else if(action == MotionEvent.ACTION_MOVE) {
            if(mObjectAnimator.isRunning()) {
                mTouchX = ev.getX();
                mTouchY = ev.getY();

                invalidateSelf();

                return true;
            }
        } else if(action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_OUTSIDE
                || action == MotionEvent.ACTION_CANCEL) {
            mTouched = false;
            invalidateSelf();

            return true;
        }

        return false;
    }

    public void setAnimationStep(float f) {
        mAnimationStep = f;
        float fi = (1f - f);

        mPaint.setAlpha((int)(sInterpolator.getInterpolation(fi) * 255));

        float scale = 0.1f + (0.9f * f);

        mMatrix.setScale(scale, scale, mCircleRadius, mCircleRadius);
        mRadial.setLocalMatrix(mMatrix);

        invalidateSelf();
    }

    private int getSemi() {
        return Color.argb(
                0x66,
                0xAA,
                0xAA,
                0xAA
        );
    }

    public float getAnimationStep() {
        return mAnimationStep;
    }

    @Override
    public void draw(Canvas canvas) {
        if(mInAnimation) {
            float startX = mTouchX - mCircleRadius;
            float startY = mTouchY - mCircleRadius;

            int saveState = canvas.save();

            canvas.translate(startX, startY);
            canvas.drawRect(0, 0, mCircleRadius * 2, mCircleRadius * 2, mPaint);

            canvas.restoreToCount(saveState);
        }

        if(mTouched) {
            canvas.drawColor(0x33AAAAAA, PorterDuff.Mode.SRC_OVER);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return 0;
    }

    public static class RippleViewCallback implements Callback{

        private RippleDrawable mDrawable;
        private View mTarget;

        public RippleViewCallback(View target) {
            mTarget = target;
        }

        public RippleDrawable getDrawable() {
            return mDrawable;
        }

        public void setDrawable(RippleDrawable mDrawable) {
            this.mDrawable = mDrawable;
        }

        @Override
        public void invalidateDrawable(Drawable who) {
            mTarget.invalidate();
        }

        @Override
        public void scheduleDrawable(Drawable who, Runnable what, long when) {

        }

        @Override
        public void unscheduleDrawable(Drawable who, Runnable what) {

        }
    }

    public void cancelAnimation() {
        if(mObjectAnimator != null && mObjectAnimator.isRunning())
            mObjectAnimator.cancel();
    }
}
