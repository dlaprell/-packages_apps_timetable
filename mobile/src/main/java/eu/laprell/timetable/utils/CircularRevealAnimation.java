package eu.laprell.timetable.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

/**
 * Created by david on 10.11.14.
 */
public class CircularRevealAnimation {

    private Matrix mMatrix;
    private float mMaskScale, mMaskX, mMaskY;
    private Paint mPaint;
    private CircularRevealObj mObj;

    private CircularRevealAnimation() {
        mMatrix = new Matrix();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public float getMaskScale() {
        return mMaskScale;
    }

    public void setMaskScale(float mMaskScale) {
        this.mMaskScale = mMaskScale;
    }

    public float getMaskX() {
        return mMaskX;
    }

    public void setMaskX(float mMaskX) {
        this.mMaskX = mMaskX;
    }

    public float getMaskY() {
        return mMaskY;
    }

    public void setMaskY(float mMaskY) {
        this.mMaskY = mMaskY;
    }

    public void interDraw(Canvas c) {
        mMatrix.setScale(1.0f / mMaskScale, 1.0f / mMaskScale);
        mMatrix.preTranslate(-getMaskX(), -getMaskY());

        mPaint.getShader().setLocalMatrix(mMatrix);

        c.translate(getMaskX(), getMaskY());
        c.scale(mMaskScale, mMaskScale);

        c.drawBitmap(mObj.getMask(), 0.0f, 0.0f, mPaint);
    }

    public static CircularRevealAnimation create(CircularRevealObj o) {
        CircularRevealAnimation a = new CircularRevealAnimation();
        a.mObj = o;

        return a;
    }

    public interface CircularRevealObj {
        public Bitmap getMask();
    }
}
