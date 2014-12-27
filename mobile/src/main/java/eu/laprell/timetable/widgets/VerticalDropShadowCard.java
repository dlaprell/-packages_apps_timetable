package eu.laprell.timetable.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import eu.laprell.timetable.utils.MetricsUtils;

/**
 * Created by david on 23.11.14.
 */
public class VerticalDropShadowCard extends FrameLayout {

    private Paint mShadowPaintTop, mShadowPaintBottom, mBackgroundPaint;
    private LinearGradient mTopShadow, mBottomShadow;

    private float mBottomStart, mTopEnd;

    private float mShadowLength = MetricsUtils.convertDpToPixel(8);
    private boolean mDrawingTopShadow = true;
    private boolean mDrawingBottomShadow = true;
    private int mBackgroundColor = Color.WHITE;

    public VerticalDropShadowCard(Context context) {
        super(context);

        init();
    }

    public VerticalDropShadowCard(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public VerticalDropShadowCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        mShadowPaintTop = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowPaintTop.setStyle(Paint.Style.FILL);

        mShadowPaintBottom = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowPaintBottom.setStyle(Paint.Style.FILL);

        initBackgroundPaint();

        setWillNotDraw(false);
    }

    private void initBackgroundPaint() {
        if(mBackgroundPaint == null)
            mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(mBackgroundColor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mTopEnd = getShadowLength();
        mBottomStart = h - getShadowLength();

        float mCenterX = w / 2f;

        mTopShadow = new LinearGradient(mCenterX, 0f, mCenterX, mTopEnd,
                Color.TRANSPARENT, Color.LTGRAY, Shader.TileMode.CLAMP);
        mBottomShadow = new LinearGradient(mCenterX, mBottomStart, mCenterX, h,
                Color.LTGRAY, Color.TRANSPARENT, Shader.TileMode.CLAMP);

        mShadowPaintTop.setShader(mTopShadow);
        mShadowPaintBottom.setShader(mBottomShadow);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0f, mDrawingTopShadow ? mTopEnd : 0f, getWidth(),
                mDrawingBottomShadow ? mBottomStart : getHeight(), mBackgroundPaint);

        if(mDrawingTopShadow) {
            canvas.drawRect(0, 0, getWidth(), mTopEnd, mShadowPaintTop);
        }

        if(mDrawingBottomShadow) {
            canvas.drawRect(0, mBottomStart, getWidth(), getHeight(), mShadowPaintBottom);
        }

        super.onDraw(canvas);
    }

    public boolean isDrawingTopShadow() {
        return mDrawingTopShadow;
    }

    public void setDrawingTopShadow(boolean mDrawingTopShadow) {
        this.mDrawingTopShadow = mDrawingTopShadow;

        invalidate();
    }

    public boolean isDrawingBottomShadow() {
        return mDrawingBottomShadow;
    }

    public void setDrawingBottomShadow(boolean mDrawingBottomShadow) {
        this.mDrawingBottomShadow = mDrawingBottomShadow;

        invalidate();
    }

    public float getShadowLength() {
        return mShadowLength;
    }

    public void setShadowLength(float mShadowLength) {
        this.mShadowLength = mShadowLength;

        invalidate();
    }

    public void setBackgroundColor(int color) {
        mBackgroundColor = color;

        initBackgroundPaint();
        invalidate();
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }
}
