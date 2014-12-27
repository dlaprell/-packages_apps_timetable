package eu.laprell.timetable.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import eu.laprell.timetable.utils.MetricsUtils;

/**
 * Created by david on 15.12.14.
 */
public class TableView extends View {

    private float mHeaderColumnWidth = MetricsUtils.convertDpToPixel(24f);
    private float mHeaderRowHeight = MetricsUtils.convertDpToPixel(32f);
    private float mSeperatorThickness = MetricsUtils.convertDpToPixel(1f);

    private float mColumnWidth, mColumnOffset;
    private float mRowHeight, mRowOffset;

    private int mNumOfColumns = 5;
    private int mNumOfRows = 8;

    private int mColorDecor = Color.LTGRAY;
    private int mColorLegend = Color.RED;
    private int mColorText = Color.GRAY;

    private Paint mPaintDecor;
    private Paint mPaintLegend;
    private Paint mPaintText;
    private Paint mPaintCellBG;
    private Paint mPaintTouchFeedback;

    private DataProvider mDataProvider;

    private GestureProcessor mGesture;
    private GestureDetectorCompat mDetector;
    private InputListener mInputListener;

    private final RectF mCellPos = new RectF();
    private final Rect mTouchCellRect = new Rect();

    private boolean mShowingTouchDown = false;
    private float mTouchX, mTouchY;

    public TableView(Context context) {
        super(context);

        init();
    }

    public TableView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public TableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        setClickable(true);

        mGesture = new GestureProcessor();
        mDetector = new GestureDetectorCompat(getContext(), mGesture);

        initDecorPaint();

        initLegendPaint();

        initTextPaint();

        initCellBgPaint();

        initTouchFeedbackPaint();

        setWillNotDraw(false);
    }

    private void initTouchFeedbackPaint() {
        mPaintTouchFeedback = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintTouchFeedback.setColor(Color.GRAY);
        mPaintTouchFeedback.setAlpha(170);
    }

    private void initCellBgPaint() {
        mPaintCellBG = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintCellBG.setStyle(Paint.Style.FILL);
    }

    private void initTextPaint() {
        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintText.setStyle(Paint.Style.FILL);
        mPaintText.setColor(mColorText);
        mPaintText.setTextSize(MetricsUtils.convertSpToPixel(18f));
        mPaintText.setTypeface(Typeface.DEFAULT_BOLD);
    }

    private void initLegendPaint() {
        mPaintLegend = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintLegend.setStyle(Paint.Style.FILL);
        mPaintLegend.setColor(mColorLegend);
        mPaintLegend.setTextSize(MetricsUtils.convertSpToPixel(18f));
        mPaintLegend.setTypeface(Typeface.DEFAULT_BOLD);
    }

    private void initDecorPaint() {
        mPaintDecor = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintDecor.setStyle(Paint.Style.FILL);
        mPaintDecor.setColor(mColorDecor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        updateAllDimens();
    }

    private void updateAllDimens() {
        calculateHeaderPos();
        calculateColumnDimens();
        calculateRowDimens();
    }

    private void calculateHeaderPos() {

    }

    private float getContentWidth() {
        return getWidth() - mHeaderColumnWidth;
    }

    private float getContentHeight() {
        return getHeight() - mHeaderRowHeight;
    }

    private void calculateColumnDimens() {
        int numOfS = Math.max(mNumOfColumns - 1, 0);
        float avW = getContentWidth() - (numOfS * mSeperatorThickness);

        mColumnWidth = avW / mNumOfColumns;
    }

    private void calculateRowDimens() {
        int numOfS = Math.max(mNumOfRows - 1, 0);
        float avH = getContentHeight() - (numOfS * mSeperatorThickness);

        mRowHeight = avH / mNumOfRows;
    }

    private float getLegendTextHeight() {
        return  -1 * mPaintLegend.ascent() + mPaintLegend.descent();
    }

    private float getContentTextHeight() {
        return  -1 * mPaintText.ascent() + mPaintText.descent();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final float h = getHeight();
        final float w = getWidth();

        canvas.drawRect(0, 0, getWidth(), mHeaderRowHeight, mPaintDecor);
        canvas.drawRect(0, mHeaderRowHeight, mHeaderColumnWidth, h, mPaintDecor);

        float pos = mHeaderColumnWidth;
        float fPos, fPos2;
        String txt;

        for (int i = 0;i < mNumOfColumns - 1;i++) {
            pos += mColumnWidth;
            canvas.drawRect(pos, mHeaderRowHeight, pos + mSeperatorThickness, h, mPaintDecor);
            pos += mSeperatorThickness;
        }

        pos = mHeaderRowHeight;
        for (int i = 0;i < mNumOfRows - 1;i++) {
            pos += mRowHeight;
            canvas.drawRect(mHeaderColumnWidth, pos, w, pos + mSeperatorThickness, mPaintDecor);
            pos += mSeperatorThickness;
        }

        pos = mHeaderColumnWidth;
        for (int i = 0;i < mNumOfColumns;i++) {
            txt = mDataProvider.getHeaderTitle(i);

            fPos = pos + (mColumnWidth / 2) - ((mPaintLegend.measureText(txt)) / 2);
            canvas.drawText(txt, fPos, getLegendTextHeight(), mPaintLegend);

            pos += mSeperatorThickness + mColumnWidth;
        }

        pos = mHeaderRowHeight;
        for (int i = 0;i < mNumOfRows;i++) {
            txt = mDataProvider.getRowTitle(i);

            fPos = pos + (mRowHeight / 2) + (getLegendTextHeight() / 2);
            fPos2 = mHeaderColumnWidth / 2 - (mPaintLegend.measureText(txt) / 2);
            canvas.drawText(txt, fPos2, fPos, mPaintLegend);

            pos += mSeperatorThickness + mRowHeight;
        }

        float posX, posY;
        posX = mHeaderColumnWidth;
        for (int x = 0;x < mNumOfColumns;x++) {
            posY = mHeaderRowHeight;

            for (int y = 0;y < mNumOfRows;y++) {
                mPaintText.setColor(mDataProvider.getTextColorAt(x, y));
                mPaintCellBG.setColor(mDataProvider.getColorAt(x, y));

                mCellPos.set(posX, posY, posX + mColumnWidth, posY + mRowHeight);
                canvas.drawRect(mCellPos, mPaintCellBG);

                txt = mDataProvider.getContentAt(x, y);

                if(txt != null) {
                    boolean changed = false;
                    while(mPaintText.measureText(txt) > mColumnWidth) {
                        txt = txt.substring(0, txt.length() - 1);

                        changed = true;
                    }

                    if(changed) {
                        txt = txt.substring(0, txt.length() - 1) + ".";
                    }

                    fPos = posX + (mColumnWidth / 2) - (mPaintText.measureText(txt) / 2);
                    fPos2 = posY + (mRowHeight / 2) + (getContentTextHeight() / 2);

                    canvas.drawText(txt, fPos, fPos2, mPaintText);
                }

                posY += mRowHeight + mSeperatorThickness;
            }
            posX += mColumnWidth + mSeperatorThickness;
        }

        if(mShowingTouchDown) {
            canvas.drawRect(mTouchCellRect, mPaintTouchFeedback);
        }
    }

    public int getDecorColor() {
        return mColorDecor;
    }

    public void setDecorColor(int mColorDecor) {
        this.mColorDecor = mColorDecor;

        initDecorPaint();
        invalidate();
    }

    public int getLegendColor() {
        return mColorLegend;
    }

    public void setLegendColor(int mColorLegend) {
        this.mColorLegend = mColorLegend;

        initLegendPaint();
        invalidate();
    }

    public int getTextColor() {
        return mColorText;
    }

    public void setTextColor(int mColorText) {
        this.mColorText = mColorText;
    }

    public int getNumOfRows() {
        return mNumOfRows;
    }

    public void setNumOfRows(int mNumOfRows) {
        this.mNumOfRows = mNumOfRows;

        updateAllDimens();
        invalidate();
    }

    public int getNumOfColumns() {
        return mNumOfColumns;
    }

    public void setNumOfColumns(int mNumOfColumns) {
        this.mNumOfColumns = mNumOfColumns;

        updateAllDimens();
        invalidate();
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        final int action = event.getAction();
        if(action == MotionEvent.ACTION_DOWN) {
            mShowingTouchDown = true;
            mTouchX = event.getX();
            mTouchY = event.getY();

            saveCellLocation(mTouchCellRect, getCellXFromXLoc(mTouchX), getCellYFromYLoc(mTouchY));

            invalidate(mTouchCellRect);
        } else if (action == MotionEvent.ACTION_UP) {
            mShowingTouchDown = false;
            mTouchX = -1;
            mTouchY = -1;
            invalidate(mTouchCellRect);
        } else if(action == MotionEvent.ACTION_MOVE) {
            mTouchX = event.getX();
            mTouchY = event.getY();
            saveCellLocation(mTouchCellRect, getCellXFromXLoc(mTouchX), getCellYFromYLoc(mTouchY));
            invalidate(mTouchCellRect);
        }

        return mDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    public DataProvider getDataProvider() {
        return mDataProvider;
    }

    public void setDataProvider(DataProvider mDataProvider) {
        this.mDataProvider = mDataProvider;
    }

    public InputListener getInputListener() {
        return mInputListener;
    }

    public void setInputListener(InputListener mInputListener) {
        this.mInputListener = mInputListener;
    }

    public Rect getCellLocationOnScreen(int x, int y) {
        int[]loc = new int[2];
        getLocationOnScreen(loc);

        Rect r = new Rect();
        r.left = (int) (loc[0] + mHeaderColumnWidth + (x * (mColumnWidth + mSeperatorThickness)));
        r.top = (int) (loc[1] + mHeaderRowHeight + (y * (mRowHeight + mSeperatorThickness)));
        r.right = (int) (r.left + mColumnWidth);
        r.bottom = (int) (r.top + mRowHeight);

        return r;
    }

    public void saveCellLocation(Rect r, int x, int y) {
        r.left = (int) (mHeaderColumnWidth + (x * (mColumnWidth + mSeperatorThickness)));
        r.top = (int) (mHeaderRowHeight + (y * (mRowHeight + mSeperatorThickness)));
        r.right = (int) (r.left + mColumnWidth);
        r.bottom = (int) (r.top + mRowHeight);
    }

    public interface DataProvider {
        public String getHeaderTitle(int pos);
        public String getRowTitle(int pos);

        public String getContentAt(int x, int y);
        public int getColorAt(int x, int y);
        public int getTextColorAt(int x, int y);
    }

    public interface InputListener {
        public boolean onClickCell(int x, int y);
        public void onLongClickCell(int x, int y);
    }

    public int getCellYFromYLoc(final float yLoc) {
        int cY = -1;
        float posY = mHeaderRowHeight;

        while (yLoc >= posY) {
            cY++;
            posY += mRowHeight + mSeperatorThickness;
        }

        return cY;
    }

    public int getCellXFromXLoc(final float xLoc) {
        int cX = -1;
        float posX = mHeaderColumnWidth;

        while (xLoc >= posX) {
            cX++;
            posX += mColumnWidth + mSeperatorThickness;
        }

        return cX;
    }

    private boolean clickToListener(int x, int y) {
        if(mInputListener != null) {
            return mInputListener.onClickCell(x, y);
        } else return false;
    }

    private void longClickToListener(int x, int y) {
        if(mInputListener != null) {
            mInputListener.onLongClickCell(x, y);
        }
    }

    private class GestureProcessor extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            final float x = e.getX();
            final float y = e.getY();

            if(y > mHeaderRowHeight && x > mHeaderColumnWidth) {
                int cX = getCellXFromXLoc(x);
                int cY = getCellYFromYLoc(y);

                return clickToListener(cX, cY);
            }

            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            final float x = e.getX();
            final float y = e.getY();

            if(y > mHeaderRowHeight && x > mHeaderColumnWidth) {
                int cX = getCellXFromXLoc(x);
                int cY = getCellYFromYLoc(y);

                longClickToListener(cX, cY);

                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }
        }
    }
}
