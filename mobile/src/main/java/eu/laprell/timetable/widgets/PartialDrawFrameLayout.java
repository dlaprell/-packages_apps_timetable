package eu.laprell.timetable.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by david on 06.11.14.
 */
public class PartialDrawFrameLayout extends FrameLayout {

    private boolean mPartialDraw;
    private final RectF mFrame = new RectF();
    private Region.Op mOp = Region.Op.XOR;

    private int mSave;

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
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if(mPartialDraw) {
            mSave = canvas.save();
            canvas.clipRect(mFrame, mOp);
        }

        super.draw(canvas);
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        //Drawing children
        if(mPartialDraw)
            canvas.restoreToCount(mSave);
        super.dispatchDraw(canvas);
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

    public Region.Op getOp() {
        return mOp;
    }

    public void setOp(Region.Op mOp) {
        this.mOp = mOp;
    }
}
