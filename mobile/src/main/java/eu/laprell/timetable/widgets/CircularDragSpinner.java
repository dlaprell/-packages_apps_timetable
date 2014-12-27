package eu.laprell.timetable.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CircularDragSpinner extends View {

    private int mCircleBackgroundColor, mTextColor, mPointerColor;
    private Paint mBackgroundPaint, mTextPaint, mPointerPaint;

    public CircularDragSpinner(Context context) {
        super(context);

        init();
    }

    public CircularDragSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public CircularDragSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    private void init() {

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


    }
}
