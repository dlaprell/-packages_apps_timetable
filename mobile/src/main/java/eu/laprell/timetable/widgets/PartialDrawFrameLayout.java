package eu.laprell.timetable.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by david on 06.11.14.
 */
public class PartialDrawFrameLayout extends FrameLayout {

    private boolean mPartialDraw;
    private RectF mFrame;

    public PartialDrawFrameLayout(Context context) {
        super(context);
        init();
    }

    public PartialDrawFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PartialDrawFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        mFrame = new RectF();
    }

    @Override
    public void draw(Canvas canvas) {
        if(mPartialDraw) {
            canvas.clipRect(mFrame.left, mFrame.top, mFrame.right, mFrame.bottom);
        }
        super.draw(canvas);
    }

    public boolean isPartialDrawEnabled() {
        return mPartialDraw;
    }

    public void setPartialDrawEnabled(boolean mPartialDraw) {
        if(this.mPartialDraw != mPartialDraw) {
            postInvalidate();
        }
        this.mPartialDraw = mPartialDraw;
    }

    public RectF getDrawingFrame() {
        return mFrame;
    }

    public void setDrawingFrame(RectF mFrame) {
        if(!this.mFrame.equals(mFrame)) {
            postInvalidate();
        }
        this.mFrame.set(mFrame);
    }
}
