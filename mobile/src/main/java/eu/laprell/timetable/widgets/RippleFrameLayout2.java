package eu.laprell.timetable.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import eu.laprell.timetable.animation.RippleDrawable;

/**
 * Created by david on 07.11.14.
 */
public class RippleFrameLayout2 extends FrameLayout {

    private RippleDrawable mRipple;

    public RippleFrameLayout2(Context context) {
        super(context);
        init();
    }

    public RippleFrameLayout2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mRipple = new RippleDrawable();

        mRipple.setCallback(new RippleDrawable.RippleViewCallback(this));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mRipple.setBounds(0, 0, w, h);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean rfb = isClickable() && mRipple.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // let animator show the animation by applying changes to view's canvas
        super.onDraw(canvas);

        mRipple.draw(canvas);
    }

    public void cancelAnimation() {
        mRipple.cancelAnimation();
    }
}
