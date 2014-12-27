package eu.laprell.timetable.animation;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * Created by david on 04.12.14.
 */
public class CircleColorDrawable extends Drawable {

    private Paint mCirclePaint;
    private int mColor;
    private float mAlpha;

    private float mCircleX, mCircleY;
    private float mRadius;

    private RectF mDrawingRect;

    public CircleColorDrawable() {
        initCirclePaint();

        mDrawingRect = new RectF();
    }

    private void initCirclePaint() {
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);


    }

    @Override
    public void draw(Canvas canvas) {
        canvas.clipRect(mDrawingRect);
        canvas.drawCircle(mCircleX, mCircleY, mRadius, mCirclePaint);
    }

    @Override
    public void setAlpha(int alpha) {
        setAlphaF(alpha / 255f);
    }

    public float getAlphaF() {
        return mAlpha;
    }

    public void setAlphaF(float mAlpha) {
        this.mAlpha = mAlpha;

        mCirclePaint.setAlpha((int)(mAlpha * 255));

        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int mColor) {
        this.mColor = mColor | 0xFF000000;

        mCirclePaint.setColor(this.mColor);
        invalidateSelf();
    }

    public float getCircleX() {
        return mCircleX;
    }

    public void setCircleX(float mCircleX) {
        this.mCircleX = mCircleX;

        invalidateSelf();
    }

    public float getCircleY() {
        return mCircleY;
    }

    public void setCircleY(float mCircleY) {
        this.mCircleY = mCircleY;

        invalidateSelf();
    }

    public float getRadius() {
        return mRadius;
    }

    public void setRadius(float mRadius) {
        this.mRadius = mRadius;

        invalidateSelf();
    }

    public void setDrawingRect(float l, float t, float r, float b) {
        mDrawingRect.set(l, t, r, b);

        invalidateSelf();
    }

    public void setDrawingRect(RectF r) {
        mDrawingRect.set(r);

        invalidateSelf();
    }

    public RectF getDrawingRect() {
        return new RectF(mDrawingRect);
    }
}
