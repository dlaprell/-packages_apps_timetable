package eu.laprell.timetable.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by david on 01.12.14.
 */
public class GeometryFrameLayout extends FrameLayout {

    private Paint mBackgroundPaint;

    private int mBackgroundColor = Color.WHITE;

    public GeometryFrameLayout(Context context) {
        super(context);

        init();
    }

    public GeometryFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public GeometryFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(mBackgroundColor);

        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
