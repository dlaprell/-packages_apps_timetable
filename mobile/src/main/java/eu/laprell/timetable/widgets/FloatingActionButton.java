package eu.laprell.timetable.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageView;

import eu.laprell.timetable.utils.MetricsUtils;

/**
 * Created by david on 20.11.14
 */
public class FloatingActionButton extends ImageView {

    private Paint mBackgroundPaint;

    private int mBackgroundColor;
    private RadialGradient mGradient;
    private Paint mShadowPaint;

    private int mCircleX, mCircleY, mCircleRadius;
    private float mShadowCircleX, mShadowCircleY, mShadowCircleRadius;

    public FloatingActionButton(Context context) {
        super(context);

        init();
    }

    public FloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        super.setScaleType(ScaleType.CENTER_INSIDE);

        setWillNotDraw(false);

        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowPaint.setDither(true);

        makeBackgroundPaint();
    }

    private void makeBackgroundPaint() {
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(mBackgroundColor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mCircleX = getPaddingLeft() + ((w - getPaddingRight()) / 2);
        mCircleY = getPaddingTop() + ((h - getPaddingBottom()) / 2);

        mShadowCircleY = mCircleY + MetricsUtils.convertDpToPixel(3);
        mShadowCircleX = mCircleX;

        final int avW = w - (getPaddingRight() + getPaddingLeft());
        final int avH = h - (getPaddingTop() + getPaddingBottom());

        mShadowCircleRadius = Math.min(avW, avH) / 2;
        mCircleRadius = (int)(mShadowCircleRadius - MetricsUtils.convertDpToPixel(3));

        mGradient = new RadialGradient(
                mCircleX,
                mCircleY,
                mShadowCircleRadius,
                new int[] {Color.TRANSPARENT, Color.TRANSPARENT, Color.GRAY, Color.TRANSPARENT},
                new float[]{0f, 0.49f, 0.5f, 1f},
                android.graphics.Shader.TileMode.CLAMP);
        mShadowPaint.setShader(mGradient);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {

        canvas.drawCircle(mShadowCircleX, mShadowCircleY, mShadowCircleRadius, mShadowPaint);
        canvas.drawCircle(mCircleX, mCircleY, mCircleRadius, mBackgroundPaint);

        // Apply image rotation before!
        super.onDraw(canvas);
    }

    @Override
    public void setBackgroundColor(int color) {
        mBackgroundColor = color | 0xFF000000;

        if(mBackgroundPaint == null)
            makeBackgroundPaint();
        else
            mBackgroundPaint.setColor(mBackgroundColor);

        invalidate();
    }

    @SuppressWarnings("ResourceType")
    @Override
    public void setBackgroundResource(int resid) {
        mBackgroundColor = getResources().getColor(resid);
    }

    @Deprecated
    @Override
    public void setBackgroundDrawable(Drawable background) {
        if(background instanceof ColorDrawable) {
            ColorDrawable c = (ColorDrawable)background;

            setBackgroundColor(c.getColor());
        }
    }
}
