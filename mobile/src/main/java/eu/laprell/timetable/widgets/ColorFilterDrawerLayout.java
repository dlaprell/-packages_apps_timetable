package eu.laprell.timetable.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;

/**
 * Created by david on 17.01.15.
 */
public class ColorFilterDrawerLayout extends DrawerLayout {

    private int mColor;
    private PorterDuff.Mode mMode;
    private final RectF mFrame = new RectF();

    private boolean mColorDrawing;

    public ColorFilterDrawerLayout(Context context) {
        super(context);
    }

    public ColorFilterDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorFilterDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        super.dispatchDraw(canvas);

        if(mColorDrawing) {
            int save = canvas.save();
            canvas.clipRect(mFrame.left, mFrame.top, mFrame.right, mFrame.bottom);
            canvas.drawColor(mColor, mMode);
            canvas.restoreToCount(save);
        }
    }

    public void setColorFilter(int color, PorterDuff.Mode mode) {
        mMode = mode;
        mColor = color;

        if(mColorDrawing)
            invalidate();
    }

    public boolean isColorDrawing() {
        return mColorDrawing;
    }

    public void setColorDrawing(boolean mColorDrawing) {
        this.mColorDrawing = mColorDrawing;

        invalidate();
    }

    public void setColorDrawingFrame(RectF mFrame) {
        if(!this.mFrame.equals(mFrame)) {
            postInvalidate();
        }
        this.mFrame.set(mFrame);
    }
}
