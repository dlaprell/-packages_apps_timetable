package com.github.adnansm.timelytextview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.github.adnansm.timelytextview.animation.TimelyEvaluator;
import com.github.adnansm.timelytextview.model.NumberUtils;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.util.Property;

import eu.laprell.timetable.utils.MetricsUtils;

public class TimelyView extends View {
    private static final float RATIO = 1f;

    private static final Property<TimelyView, float[][]> CONTROL_POINTS_PROPERTY
            = new Property<TimelyView, float[][]>(float[][].class, "controlPoints") {
        @Override
        public float[][] get(TimelyView object) {
            return object.getControlPoints();
        }

        @Override
        public void set(TimelyView object, float[][] value) {
            object.setControlPoints(value);
        }
    };

    private Paint mPaint = null;
    private final Path mPath = new Path();
    private float[][] mControlPoints = null;

    private int mTextColor = Color.BLACK;
    private int mTextStroke = (int)MetricsUtils.convertSpToPixel(1);

    private float mMinDimens;

    public TimelyView(Context context) {
        super(context);
        init();
    }

    public TimelyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimelyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public float[][] getControlPoints() {
        return mControlPoints;
    }

    public void setControlPoints(float[][] controlPoints) {
        mControlPoints = controlPoints;

        initControlPoints();

        invalidate();
    }

    private void initControlPoints() {
        if(mControlPoints != null) {
            int length = mControlPoints.length;

            mPath.reset();
            mPath.moveTo(mMinDimens * mControlPoints[0][0], mMinDimens * mControlPoints[0][1]);
            for (int i = 1; i < length; i += 3) {
                mPath.cubicTo(
                        mMinDimens * mControlPoints[i    ][0], mMinDimens * mControlPoints[i    ][1],
                        mMinDimens * mControlPoints[i + 1][0], mMinDimens * mControlPoints[i + 1][1],
                        mMinDimens * mControlPoints[i + 2][0], mMinDimens * mControlPoints[i + 2][1]
                );
            }
        }
    }

    public void setNumber(int num) {
        float[][] endPoints = NumberUtils.getControlPointsFor(num);

        setControlPoints(endPoints);
    }

    public ObjectAnimator animate(int start, int end) {
        float[][] startPoints = NumberUtils.getControlPointsFor(start);
        float[][] endPoints = NumberUtils.getControlPointsFor(end);

        return ObjectAnimator.ofObject(this, CONTROL_POINTS_PROPERTY, new TimelyEvaluator(), startPoints, endPoints);
    }

    public ObjectAnimator animate(int end) {
        float[][] startPoints = NumberUtils.getControlPointsFor(-1);
        float[][] endPoints = NumberUtils.getControlPointsFor(end);

        return ObjectAnimator.ofObject(this, CONTROL_POINTS_PROPERTY, new TimelyEvaluator(), startPoints, endPoints);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int avW = w - (getPaddingLeft() + getPaddingRight());
        int avH = h - (getPaddingTop() + getPaddingBottom());

        float newMinDimen = Math.min(avW, avH);

        if(newMinDimen != mMinDimens) {
            mMinDimens = newMinDimen;
            initControlPoints();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mControlPoints != null) {
            canvas.translate(getPaddingLeft(), getPaddingTop());
            canvas.drawPath(mPath, mPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heigthWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        int maxWidth = (int) (heigthWithoutPadding * RATIO);
        int maxHeight = (int) (widthWithoutPadding / RATIO);

        if (widthWithoutPadding > maxWidth) {
            width = maxWidth + getPaddingLeft() + getPaddingRight();
        } else {
            height = maxHeight + getPaddingTop() + getPaddingBottom();
        }

        setMeasuredDimension(width, height);
    }

    private void init() {
        // A new paint with the style as stroke.
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mTextColor);
        mPaint.setStrokeWidth(mTextStroke);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        initControlPoints();
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int mTextColor) {
        this.mTextColor = mTextColor;

        init();

        invalidate();
    }

    public int getTextStroke() {
        return mTextStroke;
    }

    public void setTextStroke(int mTextStroke) {
        this.mTextStroke = mTextStroke;

        init();

        invalidate();
    }
}
