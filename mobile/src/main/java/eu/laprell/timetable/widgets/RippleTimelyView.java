package eu.laprell.timetable.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.github.adnansm.timelytextview.TimelyView;

import eu.laprell.timetable.animation.RippleDrawable;

/**
 * Created by david on 01.12.14.
 */
public class RippleTimelyView extends TimelyView {

    private RippleDrawable mRipple;

    public RippleTimelyView(Context context) {
        super(context);

        init();
    }

    public RippleTimelyView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public RippleTimelyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

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
    public boolean dispatchTouchEvent(MotionEvent event) {
        mRipple.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mRipple.draw(canvas);
    }
}
