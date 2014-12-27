package eu.laprell.timetable.animation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by david on 21.11.14.
 */
public class CircularRevealRelativeLayout extends RelativeLayout {

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private BitmapShader mShader2;
    private Paint mPaint2;

    private int mRestoreTo;

    private float mMaskRadius, mMaskX, mMaskY, mMaskAlpha;

    private boolean mEnableCircleDraw = true;
    private boolean mCaptureContent = false;
    private boolean mHadCapture;

    private boolean mRenderOnlyHalf = true;

    public CircularRevealRelativeLayout(Context context) {
        super(context);

        init();
    }

    public CircularRevealRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public CircularRevealRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        mMaskAlpha = 1f;

        mPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint2.setDither(true);
        mPaint2.setStyle(Paint.Style.FILL);
        mPaint2.setAlpha((int) (mMaskAlpha * 255));

        setWillNotDraw(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if(mRenderOnlyHalf)
            mBitmap = Bitmap.createBitmap(w / 2, h / 2, Bitmap.Config.ARGB_8888);
        else
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        mShader2 = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mPaint2.setShader(mShader2);
        mPaint2.setAlpha((int) (mMaskAlpha * 255));

        mCanvas = new Canvas(mBitmap);
        if(mRenderOnlyHalf) {
            Matrix matrix = new Matrix();
            matrix.setScale(0.5f, 0.5f);
            mCanvas.concat(matrix);

            Matrix matrix2 = new Matrix();
            matrix2.setScale(2.0f, 2.0f);
            mShader2.setLocalMatrix(matrix2);
        }

        mRestoreTo = mCanvas.save();
    }

    @Override
    public void draw(Canvas canvas) {
        if(mEnableCircleDraw) {

            if(mCaptureContent || !mHadCapture) {
                // Clear anything that was drawn in the bitmap
                mBitmap.eraseColor(Color.TRANSPARENT);
                mCanvas.restoreToCount(mRestoreTo);

                super.draw(mCanvas);

                mHadCapture = true;
            }

            canvas.drawCircle(getMaskX(), getMaskY(), getMaskScale(), mPaint2);
        } else {
            super.draw(canvas);
        }
    }

    public float getMaskScale() {
        return mMaskRadius;
    }

    public void setMaskScale(float mMaskScale) {
        this.mMaskRadius = mMaskScale;

        invalidate();
    }

    public float getMaskX() {
        return mMaskX;
    }

    public void setMaskX(float mMaskX) {
        this.mMaskX = mMaskX;

        invalidate();
    }

    public float getMaskY() {
        return mMaskY;
    }

    public void setMaskY(float mMaskY) {
        this.mMaskY = mMaskY;

        invalidate();
    }

    public boolean isEnableCircleDraw() {
        return mEnableCircleDraw;
    }

    public void setEnableCircleDraw(boolean mEnableCircleDraw) {
        this.mEnableCircleDraw = mEnableCircleDraw;
    }

    public boolean isCaptureContent() {
        return mCaptureContent;
    }

    public void setCaptureContent(boolean mCaptureContent) {
        this.mCaptureContent = mCaptureContent;

        if(mCaptureContent)
            mHadCapture = false;
    }

    public float getMaskAlpha() {
        return mMaskAlpha;
    }

    public void setMaskAlpha(float mMaskAlpha) {
        mMaskAlpha = Math.min(1, Math.max(mMaskAlpha, 0));

        this.mMaskAlpha = mMaskAlpha;

        mPaint2.setAlpha((int) (mMaskAlpha * 255));
        invalidate();
    }

    public boolean isRenderOnlyHalf() {
        return mRenderOnlyHalf;
    }

    public void setRenderOnlyHalf(boolean mRenderOnlyHalf) {
        this.mRenderOnlyHalf = mRenderOnlyHalf;
    }
}
