package eu.laprell.timetable.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import eu.laprell.timetable.R;
import eu.laprell.timetable.utils.MetricsUtils;

/**
 * Created by david on 14.11.14.
 */
public class LessonCardView extends View implements Drawable.Callback {

    private static final int DP_NR_BOX = 48;

    private int mLessonInfoColor;
    private int mLessonNrColor;
    private int mLessonTitleColor;
    private String mLessonNr = "";
    private String mStartTime = "";
    private String mEndTime= "";
    private String mTitle = "";
    private String mAdditionalInfo = "";

    private Drawable mImage;

    private Paint mInfoPaint;
    private Paint mTitlePaint;
    private Paint mNrPaint;

    private Rect mImageRect = new Rect();

    private float mNrBoxWidth;
    private float mTextPosNr, mTextPosSTime, mTextPosETime;
    private float mTextStartNr, mTextStartSTime, mTextStartETime;
    private float mColumnLeftStart, mColumnRightStart, mColumnRightEnd;
    private float mTextSizeNr, mTextSizeAdd, mTextSizeTi;
    private float mTextPosTitle, mTextPosInfo, mTextStartInfo;

    public LessonCardView(Context context) {
        super(context);

        mLessonNrColor = getResources().getColor(R.color.accent);
        mLessonNrColor = Color.LTGRAY;

        init();
    }

    public LessonCardView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.LessonCardView,
                0, 0);

        try {
            mLessonInfoColor = a.getColor(R.styleable.LessonCardView_lessonInfoColor,
                    Color.GRAY);
            mLessonNrColor = a.getColor(R.styleable.LessonCardView_lessonNrColor,
                    getResources().getColor(R.color.accent));
            mLessonTitleColor = a.getColor(R.styleable.LessonCardView_lessonTitleColor,
                    Color.DKGRAY);
        } finally {
            a.recycle();
        }

        init();
    }

    public LessonCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.LessonCardView,
                0, 0);

        try {
            mLessonInfoColor = a.getInteger(R.styleable.LessonCardView_lessonInfoColor,
                    Color.LTGRAY);
            mLessonNrColor = a.getInteger(R.styleable.LessonCardView_lessonNrColor,
                    getResources().getColor(R.color.accent));
        } finally {
            a.recycle();
        }

        init();
    }

    private void init() {
        float nrTextSize = MetricsUtils.convertSpToPixel(40);
        float titleTextSize = MetricsUtils.convertSpToPixel(24);
        float addInfoTextSize = MetricsUtils.convertSpToPixel(14);

        mInfoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInfoPaint.setColor(mLessonInfoColor | 0xFF000000);
        mInfoPaint.setStyle(Paint.Style.FILL);
        mInfoPaint.setTextSize(addInfoTextSize);

        mTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTitlePaint.setColor(mLessonTitleColor | 0xFF000000);
        mTitlePaint.setStyle(Paint.Style.FILL);
        mTitlePaint.setTextSize(titleTextSize);

        mNrPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mNrPaint.setColor(mLessonNrColor | 0xFF000000);
        mNrPaint.setStyle(Paint.Style.FILL);
        mNrPaint.setTextSize(nrTextSize);

        setWillNotDraw(false);

        mNrBoxWidth = MetricsUtils.convertDpToPixel(DP_NR_BOX);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        calculateSizes(w, h);
    }

    public void calculateSizes(int w, int h) {
        calculateTextSizes();

        mColumnLeftStart = getPaddingLeft();
        mColumnRightStart = mColumnLeftStart + MetricsUtils.convertDpToPixel(8) + mNrBoxWidth;
        mColumnRightEnd = w - getPaddingRight();

        mTextPosNr = getPaddingTop() + mTextSizeNr;
        mTextStartNr = mColumnLeftStart + (mNrBoxWidth / 2) - (mNrPaint.measureText(mLessonNr) / 2);

        measureStartTimeBox();

        measureEndTimeBox();

        final float bottom = h - getPaddingBottom();

        mTextPosTitle = bottom;
        // text start of title is mColumnRightStart

        mTextPosInfo = mColumnRightEnd - mInfoPaint.measureText(mAdditionalInfo);
        mTextStartInfo = bottom - (mTextSizeTi / 2) + (mTextSizeAdd / 2);

        int imageBottom = (int)(bottom - (mTextSizeTi + MetricsUtils.convertDpToPixel(8)));
        mImageRect.set((int)mColumnRightStart, getPaddingTop(), (int)mColumnRightEnd, imageBottom);
    }

    private void calculateTextSizes() {
        // This are the absolute text heights of the different paints
        mTextSizeNr = -1 * mNrPaint.ascent() + mNrPaint.descent();
        mTextSizeAdd = -1 * mInfoPaint.ascent() + mInfoPaint.descent();
        mTextSizeTi = -1 * mTitlePaint.ascent() + mTitlePaint.descent();
    }

    private void measureEndTimeBox() {
        mTextPosETime = mTextPosSTime + mTextSizeAdd + MetricsUtils.convertDpToPixel(2);
        mTextStartETime = mColumnLeftStart + (mNrBoxWidth / 2) - (mInfoPaint.measureText(mEndTime) / 2);
    }

    private void measureStartTimeBox() {
        mTextPosSTime = mTextPosNr + mTextSizeAdd + MetricsUtils.convertDpToPixel(4);
        mTextStartSTime = mColumnLeftStart + (mNrBoxWidth / 2) - (mInfoPaint.measureText(mStartTime) / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int s = canvas.save();

        canvas.drawText(mLessonNr, mTextStartNr, mTextPosNr, mNrPaint);

        canvas.drawText(mStartTime, mTextStartSTime, mTextPosSTime, mInfoPaint);

        canvas.drawText(mEndTime, mTextStartETime, mTextPosETime, mInfoPaint);

        canvas.drawText(mTitle, mColumnRightStart, mTextPosTitle, mTitlePaint);

        canvas.drawText(mAdditionalInfo, mTextStartInfo, mTextPosInfo, mInfoPaint);

        if(mImage != null) {
            mImage.setBounds(mImageRect);
            canvas.clipRect(mImageRect);
            mImage.draw(canvas);
        }

        canvas.restoreToCount(s);
    }

    @Override
    public void invalidateDrawable(Drawable who) {
        if(who == mImage) {
            invalidate(mImageRect);
        }
    }
    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
    }
    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
    }

    public Rect getImageRect() {
        return new Rect(mImageRect);
    }

    public int getLessonInfoColor() {
        return mLessonInfoColor;
    }

    public void setLessonInfoColor(int mLessonInfoColor) {
        this.mLessonInfoColor = mLessonInfoColor;
    }

    public int getLessonNrColor() {
        return mLessonNrColor;
    }

    public void setLessonNrColor(int mLessonNrColor) {
        this.mLessonNrColor = mLessonNrColor;
    }

    public int getLessonNr() {
        return Integer.valueOf(mLessonNr);
    }

    public void setLessonNr(int mLessonNr) {
        if(mLessonNr == -1)
            this.mLessonNr = getContext().getString(R.string.break_first_letter);
        else
            this.mLessonNr = String.valueOf(mLessonNr);
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        if(mTitle == null) mTitle = "";
        this.mTitle = mTitle;
    }

    public Drawable getImage() {
        return mImage;
    }

    public void setImage(final Drawable mImage) {
        if(this.mImage != null && this.mImage.getCallback() == this)
            this.mImage.setCallback(null);

        this.mImage = mImage;
        mImage.setCallback(this);

        invalidate(mImageRect);
    }

    public String getAdditionalInfo() {
        return mAdditionalInfo;
    }

    public void setAdditionalInfo(String mAdditionalInfo) {
        if(mAdditionalInfo == null) mAdditionalInfo = "";
        this.mAdditionalInfo = mAdditionalInfo;
    }

    public String getStartTime() {
        return mStartTime;
    }

    public void setStartTime(String mStartTime) {
        if(mStartTime == null) mStartTime = "";
        this.mStartTime = mStartTime;
    }

    public String getEndTime() {
        return mEndTime;
    }

    public void setEndTime(String mEndTime) {
        if(mEndTime == null) mEndTime = "";
        this.mEndTime = mEndTime;
    }

    public Rect getImageLocationOnScreen() {
        Rect r = new Rect();

        int[] loc = new int[2];
        getLocationOnScreen(loc);

        r.left = loc[0] + mImageRect.left;
        r.top = loc[1] + mImageRect.top;
        r.right = loc[0] + mImageRect.right;
        r.bottom = loc[1] + mImageRect.bottom;

        Log.d("Timetable", "getImageLocationOnScreen: r=" + r.toString() + " mImageRect="
                + mImageRect.toString() + " loc[0]=" + loc[0] + " loc[1]=" + loc[1]);

        return r;
    }
}
